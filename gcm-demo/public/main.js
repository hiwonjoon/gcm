// Generated by CoffeeScript 1.7.1
(function() {
  var classTable, root;

  root = typeof exports !== "undefined" && exports !== null ? exports : this;

  if (root.debug == null) {
    root.debug = {};
  }

  classTable = {
    warrior: {
      sprite: '08sprite',
      size: {
        width: 128,
        height: 192
      },
      atk: 100,
      def: 100,
      maxHp: 100,
      maxMp: 100
    },
    wizard: {
      sprite: '11sprite',
      size: {
        width: 128,
        height: 192
      },
      atk: 140,
      def: 80,
      maxHp: 80,
      maxMp: 120
    },
    sorcerer: {
      sprite: '12sprite',
      size: {
        width: 128,
        height: 208
      },
      atk: 120,
      def: 120,
      maxHp: 80,
      maxMp: 120
    },
    monk: {
      sprite: '13sprite',
      size: {
        width: 128,
        height: 192
      },
      atk: 120,
      def: 120,
      maxHp: 120,
      maxMp: 50
    },
    priest: {
      sprite: '15sprite',
      size: {
        width: 128,
        height: 192
      },
      atk: 80,
      def: 80,
      maxHp: 80,
      maxMp: 150
    },
    elementalist: {
      sprite: '16sprite',
      size: {
        width: 128,
        height: 192
      },
      atk: 100,
      def: 100,
      maxHp: 100,
      maxMp: 100
    },
    thief: {
      sprite: '17sprite',
      size: {
        width: 192,
        height: 192
      },
      atk: 100,
      def: 80,
      maxHp: 100,
      maxMp: 120
    },
    bard: {
      sprite: '18sprite',
      size: {
        width: 128,
        height: 192
      },
      atk: 80,
      def: 80,
      maxHp: 120,
      maxMp: 120
    }
  };

  zebra.ready(function() {
    var addStaticLabel, atkBar, bar, barList, btn, classList, defBar, height, hpBar, i, image, imageLabel, imageList, k, label, labelList, lst, mpBar, panel, pic, setImage, setParameter, textField, txt, txtList, v, width, _i, _j, _len, _len1, _ref;
    if (debug.firstScene) {
      return;
    }
    eval(zebra.Import("ui", "layout"));
    root = (new zCanvas('loginCanvas', 800, 450)).root;
    classList = (function() {
      var _results;
      _results = [];
      for (k in classTable) {
        _results.push(k);
      }
      return _results;
    })();
    imageList = [];
    for (k in classTable) {
      v = classTable[k];
      image = new Image;
      image.src = "res/pc/" + v.sprite + ".png";
      width = v.size.width / 4;
      height = v.size.height / 4;
      pic = new Picture(image, 0, 0, width, height);
      v.image = pic;
      imageList.push(pic);
    }
    panel = new Panel;
    panel.setBounds(0, 0, 800, 450);
    root.add(panel);
    addStaticLabel = function(txt, x, y) {
      var staticLabel;
      staticLabel = new Label(txt);
      staticLabel.setBounds(0, 0, txt.length * 20, 30);
      staticLabel.setLocation(x, y);
      return panel.add(staticLabel);
    };
    addStaticLabel('캐릭터명을 입력하세요. (영문, 최대 12자)', 100, 30);
    addStaticLabel('클래스를 선택하세요.', 100, 130);
    textField = new TextField('', 20);
    textField.setBounds(0, 0, 200, 30);
    textField.setLocation(100, 60);
    imageLabel = new ImagePan;
    setImage = function(className) {
      var selectedClass;
      selectedClass = classTable[className];
      imageLabel.setImage(selectedClass.image);
      imageLabel.setBounds(0, 0, selectedClass.size.width / 2, selectedClass.size.height / 2);
      return imageLabel.setLocation(420, 100);
    };
    lst = new List(classList);
    lst.setBounds(0, 0, 300, 200);
    lst.setLocation(100, 160);
    lst.select(0);
    lst.bind(function(src, prev) {
      setImage(src.getSelected());
      return setParameter(src.getSelected());
    });
    txtList = ["Max HP", "Max MP", "ATK", "DEF"];
    labelList = (function() {
      var _i, _len, _results;
      _results = [];
      for (_i = 0, _len = txtList.length; _i < _len; _i++) {
        txt = txtList[_i];
        _results.push(new Label(txt));
      }
      return _results;
    })();
    for (i = _i = 0, _len = labelList.length; _i < _len; i = ++_i) {
      label = labelList[i];
      label.setBounds(0, 0, 100, 30);
      label.setLocation(420, 240 + i * 30);
      panel.add(label);
    }
    _ref = (function() {
      var _j, _results;
      _results = [];
      for (i = _j = 0; _j < 4; i = ++_j) {
        _results.push(new Progress);
      }
      return _results;
    })(), hpBar = _ref[0], mpBar = _ref[1], atkBar = _ref[2], defBar = _ref[3];
    barList = [hpBar, mpBar, atkBar, defBar];
    for (_j = 0, _len1 = barList.length; _j < _len1; _j++) {
      bar = barList[_j];
      panel.add(bar);
    }
    hpBar.setBundleView('red');
    mpBar.setBundleView('blue');
    atkBar.setBundleView('black');
    defBar.setBundleView('green');
    setParameter = function(className) {
      var selectedClass, _k, _len2, _results;
      selectedClass = classTable[className];
      imageLabel.setImage(selectedClass.image);
      imageLabel.setBounds(0, 0, selectedClass.size.width / 2, selectedClass.size.height / 2);
      imageLabel.setLocation(420, 100);
      hpBar.setBounds(0, 0, selectedClass.maxHp, 20);
      mpBar.setBounds(0, 0, selectedClass.maxMp, 20);
      atkBar.setBounds(0, 0, selectedClass.atk, 20);
      defBar.setBounds(0, 0, selectedClass.def, 20);
      _results = [];
      for (i = _k = 0, _len2 = barList.length; _k < _len2; i = ++_k) {
        bar = barList[i];
        bar.setLocation(500, 240 + i * 30);
        bar.setMaxValue(100);
        bar.setValue(100);
        _results.push(bar.setGap(0));
      }
      return _results;
    };
    setImage(lst.getSelected());
    setParameter(lst.getSelected());
    btn = new Button('입장');
    btn.setBounds(0, 0, 100, 30);
    btn.setLocation(620, 380);
    btn.bind(function(src) {
      var name, sprite;
      name = textField.getValue();
      if (name.length === 0) {
        return;
      }
      sprite = classTable[lst.getSelected()].sprite;
      socket.emit('cLogin', {
        name: name,
        sprite: sprite
      });
      $('#loginCanvas').hide();
      return $('#gameCanvas').show();
    });
    panel.add(textField);
    panel.add(lst);
    panel.add(imageLabel);
    panel.add(btn);
    return $('#gameCanvas').hide();
  });

  cc.game.onStart = function() {
    cc.view.setDesignResolutionSize(800, 450, cc.ResolutionPolicy.SHOW_ALL);
    cc.view.resizeWithBrowserSize(true);
    return cc.LoaderScene.preload(g_resources, function() {
      var firstScene;
      cc.director.setDisplayStats(false);
      g.world = new World(g.name, g.sprite);
      firstScene = debug.firstScene ? new root[debug.firstScene] : new GameScene;
      return cc.director.runScene(firstScene);
    }, this);
  };

  if (debug.firstScene) {
    cc.game.run();
  }

}).call(this);

//# sourceMappingURL=main.map
