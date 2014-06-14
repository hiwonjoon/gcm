root = exports ? this
root.debug ?= {}

classTable =
  warrior:
    sprite: '08sprite'
    size:
      width: 128
      height: 192
    atk: 100
    def: 100
    maxHp: 100
    maxMp: 100
  wizard:
    sprite: '11sprite'
    size:
      width: 128
      height: 192
    atk: 140
    def: 80
    maxHp: 80
    maxMp: 120
  sorcerer:
    sprite: '12sprite'
    size:
      width: 128
      height: 208
    atk: 120
    def: 120
    maxHp: 80
    maxMp: 120
  monk:
    sprite: '13sprite'
    size:
      width: 128
      height: 192
    atk: 120
    def: 120
    maxHp: 120
    maxMp: 50
  priest:
    sprite: '15sprite'
    size:
      width: 128
      height: 192
    atk: 80
    def: 80
    maxHp: 80
    maxMp: 150
  elementalist:
    sprite: '16sprite'
    size:
      width: 128
      height: 192
    atk: 100
    def: 100
    maxHp: 100
    maxMp: 100
  thief:
    sprite: '17sprite'
    size:
      width: 192
      height: 192
    atk: 100
    def: 80
    maxHp: 100
    maxMp: 120
  bard:
    sprite: '18sprite'
    size:
      width: 128
      height: 192
    atk: 80
    def: 80
    maxHp: 120
    maxMp: 120


zebra.ready ->
  return if debug.firstScene

  eval zebra.Import("ui", "layout")
  root = (new zCanvas 'loginCanvas', 800, 450).root

  classList = (k for k of classTable)

  imageList = []
  for k, v of classTable
    image = new Image
    image.src = "res/pc/#{v.sprite}.png"
    width = v.size.width / 4
    height = v.size.height / 4
    pic = new Picture image, 0, 0, width, height
    v.image = pic
    imageList.push pic

  panel = new Panel
  panel.setBounds 0, 0, 800, 450
  root.add panel

  addStaticLabel = (txt, x, y) ->
    staticLabel = new Label txt
    staticLabel.setBounds 0, 0, txt.length * 20, 30
    staticLabel.setLocation x, y
    panel.add staticLabel

  addStaticLabel '캐릭터명을 입력하세요. (영문, 최대 12자)', 100, 30
  addStaticLabel '클래스를 선택하세요.', 100, 130


  textField = new TextField '', 20
  textField.setBounds 0, 0, 200, 30
  textField.setLocation 100, 60

  imageLabel = new ImagePan

  setImage = (className) ->
    selectedClass = classTable[className]
    imageLabel.setImage selectedClass.image
    imageLabel.setBounds 0, 0, selectedClass.size.width / 2, selectedClass.size.height / 2
    imageLabel.setLocation 420, 100

  lst = new List classList
  lst.setBounds 0, 0, 300, 200
  lst.setLocation 100, 160

  lst.select 0
  lst.bind (src, prev) ->
    setImage src.getSelected()
    setParameter src.getSelected()

  txtList = ["Max HP", "Max MP", "ATK", "DEF"]
  labelList = (new Label txt for txt in txtList)
  for label, i in labelList
    label.setBounds 0, 0, 100, 30
    label.setLocation 420, 240 + i * 30
    panel.add label

  [hpBar, mpBar, atkBar, defBar] = (new Progress for i in [0...4])
  barList = [hpBar, mpBar, atkBar, defBar]
  panel.add bar for bar in barList

  hpBar.setBundleView 'red'
  mpBar.setBundleView 'blue'
  atkBar.setBundleView 'black'
  defBar.setBundleView 'green'


  setParameter = (className) ->
    selectedClass = classTable[className]
    imageLabel.setImage selectedClass.image
    imageLabel.setBounds 0, 0, selectedClass.size.width / 2, selectedClass.size.height / 2
    imageLabel.setLocation 420, 100

    hpBar.setBounds 0, 0, selectedClass.maxHp, 20
    mpBar.setBounds 0, 0, selectedClass.maxMp, 20
    atkBar.setBounds 0, 0, selectedClass.atk, 20
    defBar.setBounds 0, 0, selectedClass.def, 20

    for bar, i in barList
      bar.setLocation 500, 240 + i * 30
      bar.setMaxValue 100
      bar.setValue 100
      bar.setGap 0


  setImage lst.getSelected()
  setParameter lst.getSelected()


  btn = new Button '입장'
  btn.setBounds 0, 0, 100, 30
  btn.setLocation 620, 380
  btn.bind (src) ->
    name = textField.getValue()
    return if name.length == 0

    sprite = classTable[lst.getSelected()].sprite
    socket.emit 'cLogin', { name, sprite }
    $('#loginCanvas').hide()
    $('#gameCanvas').show()

  panel.add textField
  panel.add lst
  panel.add imageLabel
  panel.add btn

  $('#gameCanvas').hide()

cc.game.onStart = ->
  cc.view.setDesignResolutionSize(800, 450, cc.ResolutionPolicy.SHOW_ALL)
  cc.view.resizeWithBrowserSize(true)

  cc.LoaderScene.preload g_resources, ->
    cc.director.setDisplayStats false

    g.world = new World g.name, g.sprite
    firstScene = if debug.firstScene then new root[debug.firstScene] else new GameScene
    cc.director.runScene firstScene
  , this


if debug.firstScene
  cc.game.run()

