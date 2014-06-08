root = exports ? this

root.Character = cc.Sprite.extend
  ctor: (name, spriteImage) ->
    # animation 세팅
    texture = cc.textureCache.addImage spriteImage
    texSize = texture.getContentSize()
    width = texSize.width / 4
    height = texSize.height / 4

    animFrames = []
    for i in [0...4]
      for j in [0...4]
        frame = cc.SpriteFrame.create texture, cc.rect(width * j, height * i, width, height)
        animFrames.push frame

    @_super animFrames[0]

    createMoveAction = (begin, end) ->
      animation = cc.Animation.create animFrames[begin..end], 0.2
      animate = cc.Animate.create animation
      seq = cc.Sequence.create animate
      cc.RepeatForever.create seq

    @moveAction =
      down: createMoveAction 0, 3
      left: createMoveAction 4, 7
      right: createMoveAction 8, 11
      up: createMoveAction 12, 15

    @nowAction = @moveAction.down
    @runAction @nowAction

    size = @getContentSize()

    drawHp = cc.DrawNode.create()
    drawHp.drawRect cc.p(0, size.height + 2), cc.p(size.width, size.height + 10), cc.color(255, 0, 0, 255)

    drawShadow = cc.DrawNode.create()
    drawShadow.drawDot cc.p(size.width / 2, size.height / 20), size.width / 3, cc.color(100, 100, 100, 255)
    drawShadow.setScaleY 0.3

    label = cc.LabelTTF.create name, 'Consolas', 15
    label.color = cc.color 20, 20, 20
    label.setPosition cc.p size.width / 2, size.height + 20

    @addChild drawShadow, -1
    @addChild label, 1
    @addChild drawHp, 1

  moveUp: ->
    @stopAction @nowAction if @nowAction
    @nowAction = @moveAction.up
    @runAction @nowAction

  moveDown: ->
    @stopAction @nowAction if @nowAction
    @nowAction = @moveAction.down
    @runAction @nowAction

  setMapPos: (x, y) ->
    dX = x - @mX
    dY = y - @mY
    if dX > 0
      newAction = @moveAction.right
    else if dX < 0
      newAction = @moveAction.left
    else if dY > 0
      newAction = @moveAction.up
    else if dY < 0
      newAction = @moveAction.down

    if newAction and newAction isnt @nowAction
      @stopAction @nowAction
      @nowAction = newAction
      @runAction @nowAction
    @mX = x
    @mY = y

root.Npc = Character.extend
  ctor: (name, sprite) ->
    @name = name
    @sprite = sprite
    @_super name, "res/npc/#{sprite}.png"

root.Player = Character.extend
  ctor: (name, sprite) ->
    @name = name
    @sprite = sprite
    @_super name, "res/pc/#{sprite}.png"

root.GameLayer = cc.Layer.extend
  init: ->
    @_super()
    size = cc.director.getWinSize()

    # map 로딩
    tileMapTag = 337
    @tileMap = cc.TMXTiledMap.create res.test_tmx

    @addChild @tileMap, 0, tileMapTag
    @tileMap.anchorX = 0
    @tileMap.anchorY = 0

    # tilemap의 aritifact 없애기 위해서
    cc.director.setProjection cc.Director.PROJECTION_2D

    # keyboard 핸들러 등록
    if 'keyboard' of cc.sys.capabilities
      param =
        event: cc.EventListener.KEYBOARD
        onKeyPressed: (key, event) =>
          g.keys[key] = true
        onKeyReleased: (key, event) =>
          g.keys[key] = false
          if key == 70 # F키를 누를 경우
            socket.emit 'cStartBattle', { x: @avatar.mX, y: @avatar.mY }
      cc.eventManager.addListener param, this

    @scheduleUpdate()

    @otherPcs = {}
    @npcs = {}

    true

  map2screen: (x, y) ->

  update: (dt) ->
    size = cc.director.getWinSize()
    if g.logged == false and g.name and g.sprite
      @avatar = new Player g.name, g.sprite
      @avatar.x = size.width / 2
      @avatar.y = size.height / 2
      @avatar.mX = @avatar.x
      @avatar.mY = @avatar.y
      @addChild @avatar, 1
      g.logged = true

    packets = g.packets
    g.packets = []

    for packet in packets
      msgType = packet.msgType
      data = packet.data
      switch msgType
        when 'sLogin'
          g.sprite = data.sprite
          g.name = data.name

        when 'sPcMove'
          name = data.name
          sprite = data.sprite
          x = data.x
          y = data.y

          if name of @otherPcs
            player = @otherPcs[name]
            player.setMapPos x, y
            player.x = x
            player.y = y
          else
            player = new Player name, sprite
            player.setMapPos x, y
            player.x = x
            player.y = y
            @tileMap.addChild player, 1
            @otherPcs[name] = player

        when 'sNpcMove'
          name = data.name
          sprite = data.sprite
          x = data.x
          y = data.y

          if name of @npcs
            npc = @npcs[name]
            npc.setMapPos x, y
            npc.x = x
            npc.y = y
          else
            npc = new Npc name, sprite
            npc.setMapPos x, y
            npc.x = x
            npc.y = y
            @tileMap.addChild npc, 1
            @npcs[name] = npc

        when 'sStartBattle'
          console.log 'sStartBattle'
          if data.name of @npcs
            npc = @npcs[data.name]
            g.me = @avatar
            g.enemy = npc
            battleScene = new BattleScene
            transition = cc.TransitionProgressRadialCW.create 0.5, battleScene
            cc.director.runScene transition

        when 'sQuit'
          if quit of @otherPcs
            other = @otherPcs[quit]
            @tileMap.removeChild other
            delete @otherPcs[quit]

    unless cc.sys.isNative
      return unless @avatar

      mapSize = @tileMap.getContentSize()
      [mX, mY] = [@avatar.mX, @avatar.mY]
      [dX, dY] = [0, 0]

      dX += 3 if g.keys[cc.KEY.right] and mX < mapSize.width
      dX -= 3 if g.keys[cc.KEY.left] and mX > 0
      dY += 3 if g.keys[cc.KEY.up] and mY < mapSize.height
      dY -= 3 if g.keys[cc.KEY.down] and mY > 0

      @avatar.setMapPos mX+dX, mY+dY

      socket.emit 'cMove', { x: @avatar.mX, y: @avatar.mY } if dX != 0 or dY != 0

      mapPos = @tileMap.getPosition()
      mapPos.x = size.width / 2 - @avatar.mX
      mapPos.y = size.height / 2 - @avatar.mY
      @tileMap.setPosition mapPos

root.GameScene = cc.Scene.extend
  onEnter: ->
    this._super()
    layer = new GameLayer()
    layer.init()
    this.addChild layer
