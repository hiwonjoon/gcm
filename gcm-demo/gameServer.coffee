
fs = require 'fs'
path = require 'path'
net = require 'net'

class Player
  constructor: (@world, @socket, @name, @sprite) ->
  x: 0
  y: 0
  hp: 100
  isNpc: false
  battle: null
  move: (x, y) ->
  attack: (x, y) ->
  tick: ->

class Npc
  constructor: (@world, @name, @sprite) ->
  x: 0
  y: 0
  hp: 100
  isNpc: true
  battle: null
  testTick: 0
  move: (x, y) ->
  attack: (x, y) ->
  tick: ->
    @testTick++
    return if @battle

    if @testTick % 10 == 0
      x = @x - 2 + Math.floor( Math.random() * 5 )
      y = @y - 2 + Math.floor( Math.random() * 5 )
      @world.processNpcMove @, x, y

class Projectile
  constructor: (@world, @name, @sprite) ->
  x: 0
  y: 0
  tick: ->

class Battle
  constructor: (@world, @char1, @char2) ->
    @char1.battle = @
    @char2.battle = @
    @isPvp = @char1.isNpc or @char2.isNpc
    @charList = [@char1, @char2]

  isPvp: false
  charList: []
  attacker: null
  isDefending: false

  beginTime: Date.now()
  start: ->
    @char1.socket?.emit 'sStartBattle',
      name: @char2.name
      sprite: @char2.sprite
      isNpc: @char2.isNpc
      isFirst: true

    @char1.socket?.emit 'sBattleStatus',
      msg: 'Your Turn'
      turn: true

    @char2.socket?.emit 'sStartBattle',
      name: @char1.name
      sprite: @char1.sprite
      isNpc: @char1.isNpc
      isFirst: false

    @char2.socket?.emit 'sBattleStatus',
      msg: 'Waiting...'
      turn: false

    @attacker = @char1

  getOpponent: (char) ->
    if char == @char1 then @char2 else @char1

  processAttack: (attacker, isSkill) ->
    return unless @attacker == attacker

    maxDamage = if isSkill then 50 else 20
    damage = Math.floor Math.random() * maxDamage
    damage /= 2 if @isDefending
    @isDefending = false

    attackee = @getOpponent(attacker)
    hp = attackee.hp - damage
    hp = 0 if hp < 0
    attackee.hp = hp

    data =
      from: attacker.name
      to: attackee.name
      isSkill: isSkill
      damage: damage
      hp: hp

    console.log 'sAttack', data

    attacker.socket?.emit 'sAttack', data
    attackee.socket?.emit 'sAttack', data

    if hp > 0
      @changeTurn()
    else
      @finish attacker, attackee, false

  processDefend: (attacker) ->
    return unless @attacker == attacker

    @isDefending = true
    @changeTurn()

  changeTurn: ->
    nextAttacker = @getOpponent @attacker
    nextAttacker.socket?.emit 'sBattleStatus',
      msg: 'Your Turn'
      turn: true

    @attacker.socket?.emit 'sBattleStatus',
      msg: 'Waiting...'
      turn: false
    @attacker = nextAttacker

  processFlee: (attacker) ->
    return unless @attacker == attacker
    @finish @getOpponent(attacker), attacker, false

  aiTick: null
  tick: ->
    now = Date.now()
    if now - @beginTime > 5 * 60 * 1000
      @finish @char1, @char2, true
      return

    return unless @attacker.isNpc

    @aiTick = now unless @aiTick
    if (now - @aiTick) > (3000 + Math.random() * 2000)
      isSkill = Math.random() > 0.7
      @attacker.battle.processAttack @attacker, isSkill
      @aiTick = null

  finish: (winner, loser, isDraw) ->
    duration = Date.now() - @beginTime
    @world?.server?.sendToGcm
      msgType: 'battleResult'
      body:
        isDraw: isDraw
        duration: duration
        pos:
          x: winner.x
          y: winner.y
        winner:
          id: winner.name
          isNpc: winner.isNpc
        loser:
          id: loser.name
          isNpc: loser.isNpc
        reward:
          exp: 20
          gold: 100
        time: Date.now()

    data =
      winner: winner.name
      loser: loser.name
      isDraw: isDraw

    winner.socket?.emit 'sEndBattle', data
    loser.socket?.emit 'sEndBattle', data

    winner.hp = 100
    loser.hp = 100
    winner.battle = null
    loser.battle = null
    for battle, i in @world.battleList
      if battle == @
        @world.battleList.splice i, 1
        break

class World
  constructor: (@server) ->

  playerIdTable: {}
  playerNameTable: {}
  npcTable: {}
  projectileTable: {}
  npcSpriteList: []
  maxNpc: 30
  mapWidth: 1200 # gara
  mapHeight: 900
  index: 0

  battleList: []

  processLogin: (socket) ->
    player = @playerIdTable[socket.id]
    return unless player

    playerSocket = player.socket
    playerSocket.emit 'sLogin', { name: player.name, sprite: player.sprite }
    for k, v of @playerNameTable when v.name != player.name
      playerSocket.emit 'sPcMove', { name: v.name, sprite: v.sprite, x: v.x, y: v.y }
      v.socket.emit 'sPcMove', { name: player.name, sprite: player.sprite, x: player.x, y: player.y }

    for k, v of @npcTable
      playerSocket.emit 'sNpcMove', { name: v.name, sprite: v.sprite, x: v.x, y: v.y }

  processLogout: (socket) ->
    player = @playerIdTable[socket.id]
    return unless player

    playerSocket = player.socket
    playerSocket.broadcast.emit 'sQuit', player.name

    delete @playerIdTable[playerSocket.id]
    delete @playerNameTable[player.name]

  processPcMove: (socket, x, y) ->
    player = @playerIdTable[socket.id]
    return unless player

    playerSocket = player.socket
    player.x = x
    player.y = y
    playerSocket.broadcast.emit 'sPcMove', { name: player.name, sprite: player.sprite, x: player.x, y: player.y }


  getDistance: (char1, char2) ->
    [dX, dY] = [char1.x - char2.x, char1.y - char2.y]
    Math.sqrt dX*dX + dY*dY

  getFirstCandidate: (char, table) ->
    for k, v of table
      dist = @getDistance char, v
      if char != v and dist < 50 and v.battle == null
        return v
    null

  processStartBattle: (socket, x, y) ->
    player = @playerIdTable[socket.id]
    return unless player
    return if player.battle

    playerSocket = player.socket
    opponent = @getFirstCandidate player, @playerIdTable
    opponent = @getFirstCandidate player, @npcTable unless opponent
    return unless opponent

    battle = new Battle @, player, opponent
    @battleList.push battle
    battle.start()

  processAttack: (socket, isSkill) ->
    player = @playerIdTable[socket.id]
    return unless player
    player.battle?.processAttack(player, isSkill)

  processDefend: (socket) ->
    player = @playerIdTable[socket.id]
    return unless player
    player.battle?.processDefend player

  processFlee: (socket) ->
    player = @playerIdTable[socket.id]
    return unless player
    player.battle?.processFlee player

  processNpcMove: (npc, x, y) ->
    npc.x = x
    npc.y = y
    for k, v of @playerNameTable
      v.socket.emit 'sNpcMove', { name: npc.name, sprite: npc.sprite, x: npc.x, y: npc.y }

  processChat: (from, to, msg) ->
    player = @playerNameTable[from]
    return unless player

    playerSocket = player.socket
    playerSocket.emit 'sChat',
      from: from
      msg: msg
    playerSocket.broadcast.emit 'sChat',
      from: from
      msg: msg

  spawnNpc: ->
    rand = Math.floor(Math.random() * @npcSpriteList.length)
    npc = new Npc @, "#{@index}_test", @npcSpriteList[rand]
    npc.x = Math.floor(Math.random() * @mapWidth)
    npc.y = Math.floor(Math.random() * @mapHeight)
    @npcTable[npc.name] = npc
    @index++

    for k, v of @playerNameTable
      v.socket.emit 'sNpcMove', { name: npc.name, sprite: npc.sprite, x: npc.x, y: npc.y }

  tick: ->
    for k, v of @projectileTable
      v.tick()

    for k, v of @npcTable
      v.tick()

    for k, v of @playerIdTable
      v.tick()

    for battle in @battleList
      battle.tick()

    nowNpc = (v for k, v of @npcTable)
    if nowNpc.length < @maxNpc
      @spawnNpc()

  loadDatasheet: ->
    dataDir = path.join __dirname, 'data'
    skillTeplateCsv = path.join dataDir, 'skill.csv'
    npcTemplateCsv = path.join dataDir, 'npc.csv'
    spawnTemplateCsv = path.join dataDir, 'spawn.csv'

    loadFromCsv = (csvPath, onLoadRecord) ->
      text = fs.readFileSync csvPath, 'utf8'
      lines = text.split '\n'
      for line in lines when line.trim().length > 0
        onLoadRecord line.split ','

    skillTemplateTable = {}
    loadFromCsv skillTeplateCsv, (record) ->
      [id, name, atk, coolTime] = record
      id = parseInt id
      atk = parseInt atk
      coolTime = parseInt coolTime
      skillTemplateTable[id] = new SkillTemplate id, name, atk, coolTime

    npcTemplateTable = {}
    loadFromCsv npcTemplateCsv, (record) ->
      [id, name, sprite, hp, skillListStr] = record
      id = parseInt id
      hp = parseInt hp
      skillList = []
      for skillTemplateId in skillListStr.split ';'
        skill = skillTemplateTable[skillTemplateId]
        skillList.push skill if skill

      npcTemplateTable[id] = new NpcTemplate id, name, sprite, hp, skillList

    spawnTemplateTable = {}
    loadFromCsv spawnTemplateCsv, (record) ->
      [id, npcTemplateId, x, y, respawnMsec] = record
      id = parseInt id
      npcTemplateId = parseInt npcTemplateId
      x = parseInt x
      y = parseInt y
      respawnMsec = parseInt respawnMsec

      npcTemplate = npcTemplateTable[npcTemplateId]
      spawnTemplateTable[id] = new SpawnTemplate id, npcTemplate, x, y, respawnMsec if npcTemplate

    fileList = fs.readdirSync path.join __dirname, 'public', 'res', 'npc'
    @npcSpriteList = (file.substring(0, file.length - 4) for file in fileList when /\.png$/.test(file))


  init: ->
    @loadDatasheet()
    setInterval =>
      @tick()
    , 100

  createPlayer: (socket, name, sprite) ->
    player = new Player @, socket, name, sprite
    @playerIdTable[socket.id] = player
    @playerNameTable[player.name] = player
    @processLogin socket

class GameServer
  constructor: (@httpServer) ->

  init: ->
    @handleConnection()
    @connectToGcm '127.0.0.1', 1338
    @world = new World @
    @world.init()

  getPlayerBySocket: (socket) ->
    @world.playerIdTable[socket.id]

  handleConnection: ->
    io = require('socket.io').listen @httpServer
    io.set 'log level','1'
    io.on 'connection', (socket) =>
      console.log socket.id

      ###
          i = Math.floor Math.random() * maleNames.length
          j = Math.floor Math.random() * sprites.length
          myName = maleNames[i]
          mySprite = sprites[j]
          playerTable[socket.id] = new Player myName, mySprite
          maleNames.splice i, 1
          socket.emit 'sConnection', { name: myName, sprite: mySprite }
      ###

      socket.on 'cMove', (data) =>
        @world.processPcMove socket, data.x, data.y

        now = Date.now()
        return if now - @lastActionTime < 500
        @lastActionTime = now
        if @gcmClient
          player = @getPlayerBySocket(socket)
          @sendToGcm
            msgType: 'move'
            body:
              id: player?.name
              src: 
                x: player.x
                y: player.y
              dest:
                x: data.x
                y: data.y
              time: now

      socket.on 'cStartBattle', (data) =>
        @world.processStartBattle socket, data.x, data.y

      socket.on 'cAttack', (data) =>
        console.log 'cAttack', data
        @world.processAttack socket, data.isSkill

      socket.on 'cDefend', (data) =>
        @world.processDefend socket

      socket.on 'cFlee', (data) =>
        @world.processFlee socket

      socket.on 'cLogin', (data) =>
        @world.createPlayer socket, data.name, data.sprite, data.x, data.y

      socket.on 'cChat', (data) =>
        if @gcmClient
          @sendToGcm
            msgType: 'chat'
            body:
              user: data.from
              msg: data.msg
              time: Date.now()
        else
          @world.processChat data.from, null, data.msg

      socket.on 'disconnect', () =>
        @world.processLogout socket


  makePacket = (jsonData) ->
    body = new Buffer JSON.stringify(jsonData), 'utf8'
    header = new Buffer "#{body.length}\r\n\r\n", 'utf8'
    packet = new Buffer header.length + body.length
    header.copy packet
    body.copy packet, header.length
    packet

  sendToGcm: (jsonData) ->
    @gcmClient?.write makePacket jsonData


  gcmClient: null
  lastActionTime: 0

  onGcm: (json) ->
    switch json.msgType
      when 'chat'
        data = json.body
        @world.processChat data.user, null, data.msg

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
      #test
      @sendToGcm
        msgType: 'chat'
        body:
          user: 'John'
          msg: 'Hello, World wtf2 오호'
          time: Date.now()

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

module.exports = (server) ->
  gameServer = new GameServer server
  gameServer.init()

