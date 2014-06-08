root = exports ? this

root.BattleLayer = cc.Layer.extend
  init: ->
    @_super()
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

    true

  update: (dt) ->
    size = cc.director.getWinSize()

root.BattleScene = cc.Scene.extend
  onEnter: ->
    @_super()
    layer = new BattleLayer
    layer.init()
    this.addChild layer
