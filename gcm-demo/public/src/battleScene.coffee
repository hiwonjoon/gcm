root = exports ? this

root.debug ?= {}

root.Actions =
  attack: 0
  defend: 1
  skill: 2
  flee: 3

root.BattleStatus =
  playerTurn: 0
  cpuTurn: 1

root.BattleResult =
  draw: 0
  lose: 1
  win: 2

root.BattleLayer = cc.Layer.extend

  actions: []
  status: BattleStatus.playerTurn

  defenceMode: false

  endBattle: (result) ->
    console.log "Battle ended #{result}"
    @callAfter 4.0, =>
      cc.director.popScene()

  processPlayerAttack: ->
    win = @throwFire false
    if win
      @endBattle BattleResult.win
    else
      @callAfter 3.0, =>
        @processCpuAttack()

  processPlayerSkill: ->
    win = @throwFire true
    if win
      @endBattle BattleResult.win
    else
      @callAfter 3.0, =>
        @processCpuAttack()

  processPlayerFlee: ->
    @callAfter 4.0, =>
      cc.director.popScene()

  processPlayerDefend: ->
    @defenceMode = true
    @callAfter 3.0, =>
      @processCpuAttack()

  statusLabel: null
  setStatusText: (status) ->
    @removeChild @statusLabel if @statusLabel
    @statusLabel = cc.LabelTTF.create status, 'Consolas', 30
    @statusLabel.color = cc.color 20, 200, 200, 255
    @statusLabel.setPosition cc.p 500, 400
    @addChild @statusLabel, 2

  init: ->
    @_super()
    for k, v of Actions
      @actions[v] = k.toString().toUpperCase()

    size = cc.director.getWinSize()
    bg = cc.Sprite.create res.battle_png

    xScale = size.width / bg.width
    yScale = size.height / bg.height
    minScale = Math.min(xScale, yScale)
    bg.setScale minScale
    bg.setPosition size.width / 2, size.height / 2
    @addChild bg, -1


    @me = if g.me then new Player g.me.name, g.me.sprite  else  new Player 'mario', '08sprite'
    @enemy = if g.enemy then new Npc g.enemy.name, g.enemy.sprite else  new Player 'luigi', '17sprite'

    @me.moveUp()
    @enemy.moveDown()

    @me.setScale 3.5
    @me.setPosition size.width * 80 / 400, size.height * 30 / 238

    @enemy.setScale 2.0
    @enemy.setPosition size.width * 292 / 400, size.height * 110 / 238


    @addChild @me, 0
    @addChild @enemy, 0

    drawBounds = cc.DrawNode.create()
    drawBounds.drawRect cc.p(30, 300), cc.p(350, 430), cc.color(10, 10, 10, 255), 5, cc.color(120, 120, 120, 255)
    @addChild drawBounds, 1

    drawShadow = cc.DrawNode.create()
    #drawShadow.drawDot cc.p(100, 100), 30, cc.color('#CCFFCC')
    drawShadow.drawDot cc.p(100, 100), 30, cc.color(20, 20, 20, 255)
    #@addChild drawShadow


    for action, i of Actions
      label = cc.LabelTTF.create action.toString().toUpperCase(), 'Consolas', 15
      label.color = cc.color 230, 100, 100, 255
      label.setPosition cc.p 100, 410 - 30 * i
      label.setHorizontalAlignment cc.TEXT_ALIGNMENT_LEFT
      @addChild label, 2



    @arrow = cc.LabelTTF.create '<-', 'Consolas', 15
    @arrow.color = cc.color 255, 20, 255, 255
    @arrow.setPosition cc.p 230, 410
    @arrow.setHorizontalAlignment cc.TEXT_ALIGNMENT_LEFT
    @addChild @arrow, 2

    fadeIn = cc.FadeIn.create 0.5
    fadeOut = cc.FadeOut.create 0.5
    fadeSeq = cc.Sequence.create fadeIn, fadeOut
    forever = cc.RepeatForever.create fadeSeq
    @arrow.runAction forever

    @setStatusText 'Player turn'

    @scheduleUpdate()
    if 'keyboard' of cc.sys.capabilities
      param =
        event: cc.EventListener.KEYBOARD
        onKeyPressed: (key, event) =>
          return unless @status == BattleStatus.playerTurn
          console.log key
          switch key
            when 40 then console.log 'down'; @selectedIndex++ if @selectedIndex < 3
            when 38 then console.log 'up'; @selectedIndex-- if @selectedIndex > 0
            when 39 then console.log 'left'
            when 37 then console.log 'right'

        onKeyReleased: (key, event) =>
          return unless @status == BattleStatus.playerTurn
          console.log key
          switch key
            when 70
              console.log @selectedIndex
              switch @actions[@selectedIndex]
                when 'ATTACK' then @processPlayerAttack()
                when 'SKILL' then @processPlayerSkill()
                when 'DEFEND' then @processPlayerDefend()
                when 'FLEE' then @processPlayerFlee()

              @status = BattleStatus.cpuTurn
              @setStatusText 'Waiting...'

      cc.eventManager.addListener param, @


  attack: (from, to, isSkill) ->
    if isSkill
      emitter = cc.ParticleFire.create()
      emitter.setEmitterMode 1
    else
      emitter = cc.ParticleGalaxy.create()
      emitter.setShapeType cc.ParticleSystem.BALL_SHAPE
      emitter.setTotalParticles 100

    emitter.texture = cc.textureCache.addImage res.fire_png

    emitter.setPosition from.getPosition()
    @addChild emitter

    emitter.setScale 1
    emitter.setAutoRemoveOnFinish true
    emitter.setDuration 1
    emitter.setLife 0.5

    actionTo = cc.MoveTo.create 1, to.getPosition()
    after = cc.CallFunc.create =>
      maxDamage = if isSkill then 50 else 20
      damage = Math.floor Math.random() * maxDamage
      hp = to.hp - damage
      if hp < 0
        hp = 0

      to.setHp hp
      color = to.getColor()
      tint = cc.Sequence.create cc.TintTo.create(0.7, 255, 0, 0), cc.TintTo.create(0.3, color.r, color.g, color.b)

      damageAction = cc.Spawn.create [cc.Blink.create(1, 3), tint]
      to.runAction damageAction

    emitter.runAction cc.Sequence.create actionTo, after
    return to.hp == 0

  callAfter: (after, callback) ->
    delay = cc.DelayTime.create 3.0
    after = cc.CallFunc.create =>
      callback()
    @runAction cc.Sequence.create delay, after

  throwFire: (isSkill) ->
    @attack @me, @enemy, isSkill

  processCpuAttack: ->
    console.log 'hello'
    win = @attack @enemy, @me, (Math.random() > 0.7)
    if win
      @endBattle BattleResult.lose
    else
      @callAfter 3.0, =>
        @status = BattleStatus.playerTurn
        @setStatusText 'Player Turn'

  selectedIndex: 0

  update: (dt) ->
    size = cc.director.getWinSize()
    @arrow.setPosition cc.p 250, 410 - 30 * @selectedIndex


###


    delay = cc.DelayTime.create 10.0
    change = cc.CallFunc.create ->
      cc.director.popScene()

    #@runAction cc.Sequence.create(delay, change)

    # keyboard 핸들러 등록


###
root.BattleScene = cc.Scene.extend
  onEnter: ->
    @_super()
    layer = new BattleLayer
    layer.init()
    this.addChild layer
