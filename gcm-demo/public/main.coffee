g =
  combo: null
  classTable:
    warrior:
      sprite: '08sprite'
      atk: 100
      def: 100
      maxHp: 100
      maxMp: 100
    wizard:
      sprite: '11sprite'
      atk: 140
      def: 80
      maxHp: 80
      maxMp: 120
    sorcerer:
      sprite: '12sprite'
      atk: 120
      def: 120
      maxHp: 80
      maxMp: 120
    monk:
      sprite: '13sprite'
      atk: 120
      def: 120
      maxHp: 120
      maxMp: 50
    priest:
      sprite: '15sprite'
      atk: 80
      def: 80
      maxHp: 80
      maxMp: 150
    elementalist:
      sprite: '16sprite'
      atk: 100
      def: 100
      maxHp: 100
      maxMp: 100
    thief:
      sprite: '17sprite'
      atk: 100
      def: 80
      maxHp: 100
      maxMp: 120
    bard:
      sprite: '18sprite'
      atk: 80
      def: 80
      maxHp: 120
      maxMp: 120


zebra.ready ->
  eval zebra.Import("ui", "layout")
  root = (new zCanvas 'loginCanvas', 800, 450).root

  classList = (k for k of g.classTable)

  imageList = []
  for k, v of g.classTable
    image = new Image
    image.src = "res/pc/#{v.sprite}.png"
    width = image.naturalWidth / 4
    height = image.naturalHeight / 4
    pic = new Picture image, 0, 0, width, height
    v.image = pic
    imageList.push pic

  panel = new Panel
  panel.setBounds 0, 0, 800, 450
  root.add panel

  label = new Label('캐릭터명을 입력하세요 (영문, 최대 12자)')
  label.setBounds 0, 0, 400, 50
  label.setLocation 100, 30

  textField = new TextField '', 20
  textField.setBounds 0, 0, 200, 30
  textField.setLocation 100, 60

  imageLabel = new ImagePan imageList[0]
  imageLabel.setBounds 0, 0, 200, 200
  imageLabel.setLocation 420, 100

  lst = new List classList
  lst.setBounds 0, 0, 300, 300
  lst.setLocation 100, 100

  lst.select 0
  lst.bind (src, prev) ->
    #console.log src, prev
    console.log src.getSelected() #if prev
    imageLabel.setImage g.classTable[src.getSelected()].image

  btn = new Button '입장'
  btn.setBounds 0, 0, 100, 30
  btn.setLocation 620, 380
  btn.bind (src) ->
    name = textField.getValue()
    return if name.length == 0

    sprite = g.classTable[lst.getSelected()].sprite
    socket.emit 'cLogin', { name, sprite }
    $('#loginCanvas').hide()
    $('#gameCanvas').show()
    cc.game.run()

  panel.add label
  panel.add textField
  panel.add lst
  panel.add imageLabel
  panel.add btn

  $('#gameCanvas').hide()

cc.game.onStart = ->
  cc.view.setDesignResolutionSize(800, 450, cc.ResolutionPolicy.SHOW_ALL)
  cc.view.resizeWithBrowserSize(true)

  cc.LoaderScene.preload g_resources, ->
    cc.director.runScene new GameScene
    #cc.director.runScene new BattleScene
    #cc.director.runScene new SandboxScene
  , this

#cc.game.run()
