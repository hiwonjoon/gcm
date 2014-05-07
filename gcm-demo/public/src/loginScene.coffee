root = exports ? this

textInputGetRect = (node) ->
  rc = cc.rect node.x, node.y, node.width, node.height
  rc.x -= rc.width / 2
  rc.y -= rc.height / 2
  rc

root.LoginLayer = cc.Layer.extend
  init: ->
    if 'mouse' of cc.sys.capabilities
      cc.eventManager.addListener
        event: cc.EventListener.MOUSE
        onMouseUp: @onMouseUp
        , this

    @_trackNode = null
    @_beginPos = null

  callbackRemoveNodeWhenDidAction: (node) ->
    @removeChild node, true

  onMouseUp: (event) ->
    target = event.getCurrentTarget()
    return unless target._trackNode

    point = event.getLocation()
    rect = textInputGetRect target._trackNode

    target.onClickTrackNode cc.rectContainsPoint rect, point

  onClickTrackNode: (clicked) ->
    textField = @_trackNode
    if clicked
      textField.attachWithIME()
    else
      textField.detachWithIME()

  onEnter: ->
    @_super()
    size = cc.director.getWinSize()
    backButton = cc.MenuItemImage.create res.B1_png, res.B2_png, @backCallback, this
    selectButton = cc.MenuItemImage.create res.R1_png, res.R2_png, @selectCallback, this
    forwardButton = cc.MenuItemImage.create res.F1_png, res.F2_png, @forwardCallback, this
    menu = cc.Menu.create backButton, selectButton, forwardButton
    [menu.x, menu.y] = [0, 0]
    backButton.x = size.width / 2 - 100
    selectButton.x = size.width / 2
    forwardButton.x = size.width / 2 + 100
    backButton.y = selectButton.y = forwardButton.y = 30

    @addChild menu, 1

    @_charLimit = 20
    @_textFieldAction = cc.RepeatForever.create cc.Sequence.create cc.FadeOut.create(0.25), cc.FadeIn.create(0.25)

    @_textField = cc.TextFieldTTF.create "click and type your name", 'Arial', 36
    @addChild @_textField
    @_textField.setDelegate this
    @_textField.x = size.width / 2
    @_textField.y = size.height / 2 - 50
    @_trackNode = @_textField

    @spriteList =  ("#{i}sprite" for i in ['08', 11, 12, 13, 15, 16, 17, 18])
    @playerIndex = 0
    @changePlayer 0

  changePlayer: (offset) ->
    size = cc.director.getWinSize()

    centerX = size.width / 2
    y = size.height / 2 + 50
    leftX = centerX - 100
    rightX = centerX + 100

    if offset > 0
      [appearX, appearY] = [leftX, y]
      [disappearX, disappearY] = [rightX, y]
    else if offset < 0
      [appearX, appearY] = [rightX, y]
      [disappearX, disappearY] = [leftX, y]
    else
      [appearX, appearY] = [centerX, y]
      [disappearX, disappearY] = [centerX, y]

    duration = 0.5
    appearSeq = cc.Sequence.create(
      cc.Spawn.create(
        cc.MoveTo.create(duration, cc.p(centerX, y)),
        cc.ScaleTo.create(duration, 3.0),
        cc.FadeIn.create(duration)
      )
    )

    disappearSeq = cc.Sequence.create(
      cc.Spawn.create(
        cc.MoveTo.create(duration, cc.p(disappearX, disappearY)),
        cc.ScaleTo.create(duration, 1.0),
        cc.FadeOut.create(duration)
      ), cc.CallFunc.create(this.callbackRemoveNodeWhenDidAction, this)
    )

    oldPlayer = @player
    oldPlayer.runAction disappearSeq if oldPlayer

    @playerIndex = (@spriteList.length + @playerIndex + offset) % @spriteList.length
    @player = new Player '', @spriteList[@playerIndex]
    @addChild @player
    @player.x = appearX
    @player.y = appearY
    @player.runAction appearSeq


  #CCTextFieldDelegate
  onTextFieldAttachWithIME: (sender) ->
    unless @_action
      @_textField.runAction @_textFieldAction
      @_action = true

    false

  onTextFieldDetachWithIME: (sender) ->
    if @_action
      @_textField.stopAction @_textFieldAction
      @_textField.opacity = 255
      @_action = false

    false

  onTextFieldInsertText: (sender, text, len) ->
    return false if '\n' == text
    return true if sender.getCharCount() >= @_charLimit

    label = cc.LabelTTF.create text, 'Arial', 36
    @addChild label
    label.color = cc.color 226, 121, 7

    [endX, endY] = [sender.x, sender.y]
    endX += sender.width / 2 if sender.getCharCount()

    duration = 0.5
    label.x = endX
    label.y = cc.director.getWinSize().height - label.height * 2
    label.scale = 8

    seq = cc.Sequence.create(
      cc.Spawn.create(
        cc.MoveTo.create(duration, cc.p(endX, endY)),
        cc.ScaleTo.create(duration, 1),
        cc.FadeOut.create(duration)
      ), cc.CallFunc.create(this.callbackRemoveNodeWhenDidAction, this)
    )
    label.runAction(seq)
    false

  onTextFieldDeleteBackward: (sender, delText, len) ->
    label = cc.LabelTTF.create delText, 'Arial', 36
    @addChild label

    [beginX, beginY] = [sender.x, sender.y]
    beginX += (sender.width - label.width) / 2.0

    size = cc.director.getWinSize()
    endPos = cc.p(-size.width / 4.0, size.height * (0.5 + Math.random() / 2.0))
    duration = 1
    rotateDuration = 0.2
    repeatTime = 5
    label.x = beginX
    label.y = beginY

    seq = cc.Sequence.create(
      cc.Spawn.create(
        cc.MoveTo.create(duration, endPos),
        cc.Repeat.create(
          cc.RotateBy.create(rotateDuration, (Math.random() % 2) ? 360 : -360),
          repeatTime
        ),
        cc.FadeOut.create(duration)),
      cc.CallFunc.create(this.callbackRemoveNodeWhenDidAction, this))
    label.runAction seq

    false


  warn: (str) ->
    size = cc.director.getWinSize()
    label = cc.LabelTTF.create str, 'Arial', 24
    @addChild label
    label.x = size.width / 2
    label.y = size.height / 2

    seq = cc.Sequence.create(
      cc.DelayTime.create(1),
      cc.FadeOut.create(1),
      cc.CallFunc.create(this.callbackRemoveNodeWhenDidAction, this)
    )
    label.runAction seq

  backCallback: ->
    @changePlayer -1

  selectCallback: ->
    name = @_textField.getString()
    if name.trim().length == 0
      @warn '캐릭터 이름을 입력하세요'
      return

    sprite = @spriteList[@playerIndex]
    socket.emit 'cLogin', { name, sprite }
    cc.director.runScene new GameScene

  forwardCallback: ->
    @changePlayer 1

  onDraw: (sender) ->
    false

root.LoginScene = cc.Scene.extend
  onEnter: ->
    this._super()
    layer = new LoginLayer()
    layer.init()
    this.addChild layer
