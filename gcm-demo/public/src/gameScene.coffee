root = exports ? this

root.Player = cc.Sprite.extend
  ctor: (name, sprite) ->
    # animation 세팅
    texture = cc.textureCache.addImage "res/#{sprite}.png"
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

root.GameLayer = cc.Layer.extend
  init: ->
    @_super()
    size = cc.director.getWinSize()

    # map 로딩
    tileMapTag = 337
    @tileMap = cc.TMXTiledMap.create res.Test_tmx

    @addChild @tileMap, 0, tileMapTag
    @tileMap.anchorX = 0
    @tileMap.anchorY = 0

    # tilemap의 aritifact 없애기 위해서
    cc.director.setProjection cc.Director.PROJECTION_2D

    # keyboard 핸들러 등록
    if 'keyboard' of cc.sys.capabilities
      param =
        event: cc.EventListener.KEYBOARD
        onKeyPressed: (key, event) ->
          g.keys[key] = true
        onKeyReleased: (key, event) ->
          g.keys[key] = false
      cc.eventManager.addListener param, this

    @scheduleUpdate()

    @others = {}

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

    moves = g.moves
    g.moves = []

    for move in moves
      name = move.name
      sprite = move.sprite
      x = move.x
      y = move.y

      if name of @others
        player = @others[name]
        player.setMapPos x, y
        player.x = x
        player.y = y
      else
        player = new Player name, sprite
        player.setMapPos x, y
        player.x = x
        player.y = y
        @tileMap.addChild player, 1
        @others[name] = player

    quits = g.quits
    g.quits = []

    for quit in quits
      if quit of @others
        other = @others[quit]
        @tileMap.removeChild other
        delete @others[quit]

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
