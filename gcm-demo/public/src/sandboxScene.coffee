###
  var DemoFirework = ParticleDemo.extend({
    onEnter:function () {
        this._super();

        this._emitter = cc.ParticleFireworks.create();
        this._background.addChild(this._emitter, 10);
        var myTexture = cc.textureCache.addImage(s_stars1);
        this._emitter.texture = myTexture;
        if (this._emitter.setShapeType)
            this._emitter.setShapeType(cc.ParticleSystem.STAR_SHAPE);
        this.setEmitterPosition();
    },
    title:function () {
        return "ParticleFireworks";
    }
});
###

root = exports ? this


root.SandNpc = cc.Sprite.extend
  ctor: (name, sprite) ->
    # animation 세팅
    texture = cc.textureCache.addImage sprite
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


root.SandboxLayer = cc.LayerRGBA.extend
  init: ->
    size = cc.director.getWinSize()

    for key of res
      continue

      fileName = res[key]
      if /res\/npc\//.test fileName
        cc.log key
        x = Math.random() * size.width
        y = Math.random() * size.height

        npc = new SandNpc key, fileName
        npc.x = x
        npc.y = y

        npcSize = npc.getContentSize()

        drawHp = cc.DrawNode.create()
        drawHp.drawRect cc.p(0, npcSize.height + 2), cc.p(npcSize.width, npcSize.height + 10), cc.color(255, 0, 0, 255)

        drawShadow = cc.DrawNode.create()
        drawShadow.drawDot cc.p(npcSize.width / 2, npcSize.height / 20), npcSize.width / 3, cc.color(100, 100, 100, 255)
        drawShadow.setScaleY 0.3

        label = cc.LabelTTF.create key, 'Consolas', 10
        label.color = cc.color 200, 200, 200
        label.setPosition cc.p npcSize.width / 2, npcSize.height + 20

        npc.addChild drawShadow, -1
        npc.addChild label, 1
        npc.addChild drawHp, 1

        @addChild npc


    for i in [0..10]
      emitter = cc.ParticleFire.create()
      emitter.texture = cc.textureCache.addImage res.fire_png
      emitter.setEmitterMode(1)

      x = 100 # Math.random() * 300
      y = 200 + i * 30 #  Math.random() * 200
      toX = 500 #x + Math.random() * 400
      toY = y

      emitter.x = x
      emitter.y = y
      @addChild emitter
      emitter.setScale 0.3


      cc.log emitter.autoRemoveOnFinish
      cc.log emitter.duration
      cc.log emitter.startColor
      cc.log emitter.endColor
      cc.log emitter.speed
      cc.log emitter.life
      cc.log emitter.totalParticles
      cc.log 'wow'
      cc.log emitter.getContentSize().width
      cc.log emitter.getContentSize().height

      #emitter.setAutoRemoveOnFinish true
      emitter.setDuration 0.2 #* i
      emitter.setLife emitter.life * 0.1 * i
      emitter.setStartColor cc.color(0, 0, 255, 255)
      emitter.setEndColor cc.color(0, 255, 0, 255)






      #actionTo = cc.JumpTo.create(1, cc.p(toX, toY), 100, 1)
      actionTo = cc.MoveTo.create(1, cc.p(toX, toY))
      emitter.runAction actionTo

###
    actionBy = cc.JumpBy.create(2, cc.p(300, 0), 50, 4)
    actionUp = cc.JumpBy.create(2, cc.p(0, 0), 80, 4)
    actionByBack = actionBy.reverse();
    delay = cc.DelayTime.create(0.25);

    this._tamara.runAction(actionTo);
    this._grossini.runAction(cc.Sequence.create(actionBy, delay, actionByBack));
    this._kathia.runAction(cc.RepeatForever.create(
      cc.Sequence.create(actionUp, delay.clone() )
    ) );
###

root.SandboxScene = cc.Scene.extend
  onEnter: ->
    this._super()
    layer = new SandboxLayer
    layer.init()

    whiteLayer = cc.LayerColor.create cc.color(255, 255, 255, 255)
    this.addChild layer
    #this.addChild whiteLayer, -3
