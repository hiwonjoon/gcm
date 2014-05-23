// Generated by CoffeeScript 1.6.3
(function() {
  var root, textInputGetRect;

  root = typeof exports !== "undefined" && exports !== null ? exports : this;

  textInputGetRect = function(node) {
    var rc;
    rc = cc.rect(node.x, node.y, node.width, node.height);
    rc.x -= rc.width / 2;
    rc.y -= rc.height / 2;
    return rc;
  };

  root.LoginLayer = cc.Layer.extend({
    init: function() {
      if ('mouse' in cc.sys.capabilities) {
        cc.eventManager.addListener({
          event: cc.EventListener.MOUSE,
          onMouseUp: this.onMouseUp
        }, this);
      }
      this._trackNode = null;
      return this._beginPos = null;
    },
    callbackRemoveNodeWhenDidAction: function(node) {
      return this.removeChild(node, true);
    },
    onMouseUp: function(event) {
      var point, rect, target;
      target = event.getCurrentTarget();
      if (!target._trackNode) {
        return;
      }
      point = event.getLocation();
      rect = textInputGetRect(target._trackNode);
      return target.onClickTrackNode(cc.rectContainsPoint(rect, point));
    },
    onClickTrackNode: function(clicked) {
      var textField;
      textField = this._trackNode;
      if (clicked) {
        return textField.attachWithIME();
      } else {
        return textField.detachWithIME();
      }
    },
    onEnter: function() {
      var backButton, forwardButton, i, menu, selectButton, size, _ref;
      this._super();
      size = cc.director.getWinSize();
      backButton = cc.MenuItemImage.create(res.b1_png, res.b2_png, this.backCallback, this);
      selectButton = cc.MenuItemImage.create(res.r1_png, res.r2_png, this.selectCallback, this);
      forwardButton = cc.MenuItemImage.create(res.f1_png, res.f2_png, this.forwardCallback, this);
      menu = cc.Menu.create(backButton, selectButton, forwardButton);
      _ref = [0, 0], menu.x = _ref[0], menu.y = _ref[1];
      backButton.x = size.width / 2 - 100;
      selectButton.x = size.width / 2;
      forwardButton.x = size.width / 2 + 100;
      backButton.y = selectButton.y = forwardButton.y = 30;
      this.addChild(menu, 1);
      this._charLimit = 20;
      this._textFieldAction = cc.RepeatForever.create(cc.Sequence.create(cc.FadeOut.create(0.25), cc.FadeIn.create(0.25)));
      this._textField = cc.TextFieldTTF.create("click and type your name", 'Arial', 36);
      this.addChild(this._textField);
      this._textField.setDelegate(this);
      this._textField.x = size.width / 2;
      this._textField.y = size.height / 2 - 50;
      this._trackNode = this._textField;
      this.spriteList = (function() {
        var _i, _len, _ref1, _results;
        _ref1 = ['08', 11, 12, 13, 15, 16, 17, 18];
        _results = [];
        for (_i = 0, _len = _ref1.length; _i < _len; _i++) {
          i = _ref1[_i];
          _results.push("" + i + "sprite");
        }
        return _results;
      })();
      this.playerIndex = 0;
      return this.changePlayer(0);
    },
    changePlayer: function(offset) {
      var appearSeq, appearX, appearY, centerX, disappearSeq, disappearX, disappearY, duration, leftX, oldPlayer, rightX, size, y, _ref, _ref1, _ref2, _ref3, _ref4, _ref5;
      size = cc.director.getWinSize();
      centerX = size.width / 2;
      y = size.height / 2 + 50;
      leftX = centerX - 100;
      rightX = centerX + 100;
      if (offset > 0) {
        _ref = [leftX, y], appearX = _ref[0], appearY = _ref[1];
        _ref1 = [rightX, y], disappearX = _ref1[0], disappearY = _ref1[1];
      } else if (offset < 0) {
        _ref2 = [rightX, y], appearX = _ref2[0], appearY = _ref2[1];
        _ref3 = [leftX, y], disappearX = _ref3[0], disappearY = _ref3[1];
      } else {
        _ref4 = [centerX, y], appearX = _ref4[0], appearY = _ref4[1];
        _ref5 = [centerX, y], disappearX = _ref5[0], disappearY = _ref5[1];
      }
      duration = 0.5;
      appearSeq = cc.Sequence.create(cc.Spawn.create(cc.MoveTo.create(duration, cc.p(centerX, y)), cc.ScaleTo.create(duration, 3.0), cc.FadeIn.create(duration)));
      disappearSeq = cc.Sequence.create(cc.Spawn.create(cc.MoveTo.create(duration, cc.p(disappearX, disappearY)), cc.ScaleTo.create(duration, 1.0), cc.FadeOut.create(duration)), cc.CallFunc.create(this.callbackRemoveNodeWhenDidAction, this));
      oldPlayer = this.player;
      if (oldPlayer) {
        oldPlayer.runAction(disappearSeq);
      }
      this.playerIndex = (this.spriteList.length + this.playerIndex + offset) % this.spriteList.length;
      this.player = new Player('', this.spriteList[this.playerIndex]);
      this.addChild(this.player);
      this.player.x = appearX;
      this.player.y = appearY;
      return this.player.runAction(appearSeq);
    },
    onTextFieldAttachWithIME: function(sender) {
      if (!this._action) {
        this._textField.runAction(this._textFieldAction);
        this._action = true;
      }
      return false;
    },
    onTextFieldDetachWithIME: function(sender) {
      if (this._action) {
        this._textField.stopAction(this._textFieldAction);
        this._textField.opacity = 255;
        this._action = false;
      }
      return false;
    },
    onTextFieldInsertText: function(sender, text, len) {
      var duration, endX, endY, label, seq, _ref;
      if ('\n' === text) {
        return false;
      }
      if (sender.getCharCount() >= this._charLimit) {
        return true;
      }
      label = cc.LabelTTF.create(text, 'Arial', 36);
      this.addChild(label);
      label.color = cc.color(226, 121, 7);
      _ref = [sender.x, sender.y], endX = _ref[0], endY = _ref[1];
      if (sender.getCharCount()) {
        endX += sender.width / 2;
      }
      duration = 0.5;
      label.x = endX;
      label.y = cc.director.getWinSize().height - label.height * 2;
      label.scale = 8;
      seq = cc.Sequence.create(cc.Spawn.create(cc.MoveTo.create(duration, cc.p(endX, endY)), cc.ScaleTo.create(duration, 1), cc.FadeOut.create(duration)), cc.CallFunc.create(this.callbackRemoveNodeWhenDidAction, this));
      label.runAction(seq);
      return false;
    },
    onTextFieldDeleteBackward: function(sender, delText, len) {
      var beginX, beginY, duration, endPos, label, repeatTime, rotateDuration, seq, size, _ref, _ref1;
      label = cc.LabelTTF.create(delText, 'Arial', 36);
      this.addChild(label);
      _ref = [sender.x, sender.y], beginX = _ref[0], beginY = _ref[1];
      beginX += (sender.width - label.width) / 2.0;
      size = cc.director.getWinSize();
      endPos = cc.p(-size.width / 4.0, size.height * (0.5 + Math.random() / 2.0));
      duration = 1;
      rotateDuration = 0.2;
      repeatTime = 5;
      label.x = beginX;
      label.y = beginY;
      seq = cc.Sequence.create(cc.Spawn.create(cc.MoveTo.create(duration, endPos), cc.Repeat.create(cc.RotateBy.create(rotateDuration, (_ref1 = Math.random() % 2) != null ? _ref1 : {
        360: -360
      }), repeatTime), cc.FadeOut.create(duration)), cc.CallFunc.create(this.callbackRemoveNodeWhenDidAction, this));
      label.runAction(seq);
      return false;
    },
    warn: function(str) {
      var label, seq, size;
      size = cc.director.getWinSize();
      label = cc.LabelTTF.create(str, 'Arial', 24);
      this.addChild(label);
      label.x = size.width / 2;
      label.y = size.height / 2;
      seq = cc.Sequence.create(cc.DelayTime.create(1), cc.FadeOut.create(1), cc.CallFunc.create(this.callbackRemoveNodeWhenDidAction, this));
      return label.runAction(seq);
    },
    backCallback: function() {
      return this.changePlayer(-1);
    },
    selectCallback: function() {
      var name, sprite;
      name = this._textField.getString();
      if (name.trim().length === 0) {
        this.warn('캐릭터 이름을 입력하세요');
        return;
      }
      sprite = this.spriteList[this.playerIndex];
      socket.emit('cLogin', {
        name: name,
        sprite: sprite
      });
      return cc.director.runScene(new GameScene);
    },
    forwardCallback: function() {
      return this.changePlayer(1);
    },
    onDraw: function(sender) {
      return false;
    }
  });

  root.LoginScene = cc.Scene.extend({
    onEnter: function() {
      var layer;
      this._super();
      layer = new LoginLayer();
      layer.init();
      return this.addChild(layer);
    }
  });

}).call(this);

/*
//@ sourceMappingURL=loginScene.map
*/
