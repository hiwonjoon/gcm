net = require 'net'
sys = require 'sys'

class Bbs
  @generateId: -> @count++
  @count: 0

class IdGenerator
  @generateId: (prefix) -> "#{prefix}_#{@count++}"
  @count: 0

class Character
  constructor: (@world, prefix) ->
    size = @world.size
    @x = Math.floor Math.random() * size.width
    @y = Math.floor Math.random() * size.height
    @id = IdGenerator.generateId prefix

  isBound: false
  id: null
  isNpc: false
  x: 0
  y: 0

  tick: (now) ->

class Npc extends Character
  constructor: (world) ->
    super world, 'NPC'
  tick: (now) ->
  isNpc: true

class Pc extends Character
  constructor: (world) ->
    @latActionTime = (new Date).getTime() + Math.random() * 300
    super world, 'PC'

  isNpc: false
  opponent: null

  attackNpc: ->
    for npc in @world.npcs when not npc.isBound
      @opponent = npc
      @opponent.isBound = true
      @isBound = true
      @world.sendBattleResult false, 1000, @x, @y, @, npc, 100, 10

  attackPc: ->
    for pc in @world.pcs when not pc.isBound
      @opponent = pc
      @opponent.isBound = true
      @isBound = true
      @world.sendBattleResult false, 1000, @x, @y, @, pc, 100, 10

  chat: ->
    @world.sendChat @id, '', 'Hello, World'

  whisper: ->
    for pc in @world.pcs when not pc.isBound
      @opponent = pc
      @opponent.isBound = true
      @isBound = true
      @world.sendChat @id, pc.id, 'How are You?'

  write: ->
    @world.sendBbs Bbs.generateId(), @id, 'Title', 'Contents'

  wander: ->
    newX = @x + Math.floor(Math.random() * 4) - 2
    newY = @y + Math.floor(Math.random() * 4) - 2
    return if newX < 0 or newX > @world.width
    return if newY < 0 or newY > @world.height
    @world.sendMove @id, @x, @y, newX, newY
    @x = newX
    @y = newY

  lastActionTime: 0

  tick: (now) ->
    return if (now - @lastActionTime) < 1000

    @lastActionTime = now
    @isBound = false
    if @opponent
      @opponent.isBound = false
      @oponent = null

    rand = Math.floor Math.random() * 10
    switch rand
      when 0 then @attackNpc()
      when 1 then @attackPc()
      when 2 then @chat()
      when 3 then @whisper()
      when 4 then @write()
      when 5 then @wander()

class Bot extends Pc
  constructor: (world) ->
    super world

  lastActionTime: 0
  tick: (now) ->
    return if now - @lastActionTime < 1000 # 1초마다 1번 Npc 공격만 반복
    @attackNpc()

class World
  constructor: ->

  size:
    width: 800
    height: 800

  npcs: []
  pcs: []
  bots: []

  start: ->
    @connectToGcm '127.0.0.1', 1338
    @startTick()

  startTick: ->
    @tick()

  tick: ->
    now = (new Date).getTime()
    for pc in @pcs
      pc.tick now

    for bot in @bots
      bot.tick now

    for npc in @npcs
      npc.tick now

    setTimeout =>
      @tick()
    , 30

  gcmClient: null

  init: (cfg) ->

    @size.width = cfg.worldWidth
    @size.height = cfg.worldHeight

    @npcs = []
    for i in [0...cfg.pc]
      pc = new Pc @
      @pcs.push pc

    for i in [0...cfg.npc]
      npc = new Npc @
      @npcs.push npc

    for i in [0...cfg.bot]
      bot = new Bot @
      @bots.push bot

  makePacket: (jsonData) ->
    body = new Buffer JSON.stringify(jsonData), 'utf8'
    header = new Buffer "#{body.length}\r\n\r\n", 'utf8'
    packet = new Buffer header.length + body.length
    header.copy packet
    body.copy packet, header.length
    packet

  sendChat: (from, to, msg) ->
    @sendToGcm
      msgType: 'chat'
      body:
        user: from
        msg: msg
        time: (Date.now())

  sendBbs: (id, name, title, txt) ->
    @sendToGcm
      msgType: 'bbs'
      body:
        id: id
        name: name
        title: title
        txt: txt

  sendMove: (id, srcX, srcY, destX, destY) ->
    @sendToGcm
      msgType: 'move'
      body:
        id: id
        src:
          x: srcX
          y: srcY
        dest:
          x: destX
          y: destY
				time: Date.now()

  sendBattleResult: (isDraw, duration, posX, posY, winner, loser, exp, gold) ->
    @sendToGcm
      msgType: 'battleResult'
      body:
        isDraw: isDraw
        duration: duration
        pos:
          x: posX
          y: posY
        winner:
          id: winner.id
          isNpc: winner.isNpc
        loser:
          id: loser.id
          isNpc: loser.isNpc
        reward:
          exp: exp
          gold: gold
				time: Date.now()

  sendToGcm: (jsonData) ->
    @gcmClient.write @makePacket jsonData if @gcmClient
    console.log jsonData

  onGcm: (json) ->
    switch json.msgType
      when 'chat'
        data = json.body
        console.log [
          data.from
          data.to
          data.msg
        ]

  connectToGcm: (ip, port) ->
    client = net.createConnection port, ip
    buf = new Buffer 0

    client.on 'error', (err) =>
      console.log 'Error!!! try to reconnect after 10 secs'
      setTimeout =>
        @connectToGcm ip, port
      , 10000

    client.on 'connect', =>
      console.log 'Connected'
      @gcmClient = client

    client.on 'data', (data) =>
      newBuf = new Buffer (buf.length + data.length)
      buf.copy newBuf
      data.copy newBuf, buf.length
      buf = newBuf

      loop
        str = buf.toString 'utf8'
        match = /^(\d+)\r?\n\r?\n/.exec str
        break unless match

        matchSize = match[0].length
        jsonSize = parseInt(match[1])
        nextPos = match.index + matchSize + jsonSize
        break if buf.length < nextPos

        jsonBegin = match.index + matchSize
        jsonEnd = jsonBegin + jsonSize
        jsonStr = buf.toString 'utf8', jsonBegin, jsonEnd

        json = JSON.parse jsonStr
        @onGcm json

        buf = buf.slice nextPos

    client.on 'close', =>
      console.log 'Connection closed'


gcmPerf = new World
gcmPerf.init
  pc: 100
  bot: 10
  npc: 1000
  worldWidth: 1600
  worldHeight: 1600

gcmPerf.start()


#stdin = process.openStdIn
