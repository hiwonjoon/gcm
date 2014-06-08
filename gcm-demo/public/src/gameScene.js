// Generated by CoffeeScript 1.7.1
(function() {
  var root;

  root = typeof exports !== "undefined" && exports !== null ? exports : this;

  root.Character = cc.Sprite.extend({
    ctor: function(name, spriteImage) {
      var animFrames, createMoveAction, drawHp, drawShadow, frame, height, i, j, label, size, texSize, texture, width, _i, _j;
      texture = cc.textureCache.addImage(spriteImage);
      texSize = texture.getContentSize();
      width = texSize.width / 4;
      height = texSize.height / 4;
      animFrames = [];
      for (i = _i = 0; _i < 4; i = ++_i) {
        for (j = _j = 0; _j < 4; j = ++_j) {
          frame = cc.SpriteFrame.create(texture, cc.rect(width * j, height * i, width, height));
          animFrames.push(frame);
        }
      }
      this._super(animFrames[0]);
      createMoveAction = function(begin, end) {
        var animate, animation, seq;
        animation = cc.Animation.create(animFrames.slice(begin, +end + 1 || 9e9), 0.2);
        animate = cc.Animate.create(animation);
        seq = cc.Sequence.create(animate);
        return cc.RepeatForever.create(seq);
      };
      this.moveAction = {
        down: createMoveAction(0, 3),
        left: createMoveAction(4, 7),
        right: createMoveAction(8, 11),
        up: createMoveAction(12, 15)
      };
      this.nowAction = this.moveAction.down;
      this.runAction(this.nowAction);
      size = this.getContentSize();
      drawHp = cc.DrawNode.create();
      drawHp.drawRect(cc.p(0, size.height + 2), cc.p(size.width, size.height + 10), cc.color(255, 0, 0, 255));
      drawShadow = cc.DrawNode.create();
      drawShadow.drawDot(cc.p(size.width / 2, size.height / 20), size.width / 3, cc.color(100, 100, 100, 255));
      drawShadow.setScaleY(0.3);
      label = cc.LabelTTF.create(name, 'Consolas', 15);
      label.color = cc.color(20, 20, 20);
      label.setPosition(cc.p(size.width / 2, size.height + 20));
      this.addChild(drawShadow, -1);
      this.addChild(label, 1);
      return this.addChild(drawHp, 1);
    },
    moveUp: function() {
      if (this.nowAction) {
        this.stopAction(this.nowAction);
      }
      this.nowAction = this.moveAction.up;
      return this.runAction(this.nowAction);
    },
    moveDown: function() {
      if (this.nowAction) {
        this.stopAction(this.nowAction);
      }
      this.nowAction = this.moveAction.down;
      return this.runAction(this.nowAction);
    },
    setMapPos: function(x, y) {
      var dX, dY, newAction;
      dX = x - this.mX;
      dY = y - this.mY;
      if (dX > 0) {
        newAction = this.moveAction.right;
      } else if (dX < 0) {
        newAction = this.moveAction.left;
      } else if (dY > 0) {
        newAction = this.moveAction.up;
      } else if (dY < 0) {
        newAction = this.moveAction.down;
      }
      if (newAction && newAction !== this.nowAction) {
        this.stopAction(this.nowAction);
        this.nowAction = newAction;
        this.runAction(this.nowAction);
      }
      this.mX = x;
      return this.mY = y;
    }
  });

  root.Npc = Character.extend({
    ctor: function(name, sprite) {
      this.name = name;
      this.sprite = sprite;
      return this._super(name, "res/npc/" + sprite + ".png");
    }
  });

  root.Player = Character.extend({
    ctor: function(name, sprite) {
      this.name = name;
      this.sprite = sprite;
      return this._super(name, "res/pc/" + sprite + ".png");
    }
  });

  root.GameLayer = cc.Layer.extend({
    init: function() {
      var param, size, tileMapTag;
      this._super();
      size = cc.director.getWinSize();
      tileMapTag = 337;
      this.tileMap = cc.TMXTiledMap.create(res.test_tmx);
      this.addChild(this.tileMap, 0, tileMapTag);
      this.tileMap.anchorX = 0;
      this.tileMap.anchorY = 0;
      cc.director.setProjection(cc.Director.PROJECTION_2D);
      if ('keyboard' in cc.sys.capabilities) {
        param = {
          event: cc.EventListener.KEYBOARD,
          onKeyPressed: (function(_this) {
            return function(key, event) {
              return g.keys[key] = true;
            };
          })(this),
          onKeyReleased: (function(_this) {
            return function(key, event) {
              g.keys[key] = false;
              if (key === 70) {
                return socket.emit('cStartBattle', {
                  x: _this.avatar.mX,
                  y: _this.avatar.mY
                });
              }
            };
          })(this)
        };
        cc.eventManager.addListener(param, this);
      }
      this.scheduleUpdate();
      this.otherPcs = {};
      this.npcs = {};
      return true;
    },
    map2screen: function(x, y) {},
    update: function(dt) {
      var battleScene, dX, dY, data, mX, mY, mapPos, mapSize, msgType, name, npc, other, packet, packets, player, size, sprite, transition, x, y, _i, _len, _ref, _ref1;
      size = cc.director.getWinSize();
      if (g.logged === false && g.name && g.sprite) {
        this.avatar = new Player(g.name, g.sprite);
        this.avatar.x = size.width / 2;
        this.avatar.y = size.height / 2;
        this.avatar.mX = this.avatar.x;
        this.avatar.mY = this.avatar.y;
        this.addChild(this.avatar, 1);
        g.logged = true;
      }
      packets = g.packets;
      g.packets = [];
      for (_i = 0, _len = packets.length; _i < _len; _i++) {
        packet = packets[_i];
        msgType = packet.msgType;
        data = packet.data;
        switch (msgType) {
          case 'sLogin':
            g.sprite = data.sprite;
            g.name = data.name;
            break;
          case 'sPcMove':
            name = data.name;
            sprite = data.sprite;
            x = data.x;
            y = data.y;
            if (name in this.otherPcs) {
              player = this.otherPcs[name];
              player.setMapPos(x, y);
              player.x = x;
              player.y = y;
            } else {
              player = new Player(name, sprite);
              player.setMapPos(x, y);
              player.x = x;
              player.y = y;
              this.tileMap.addChild(player, 1);
              this.otherPcs[name] = player;
            }
            break;
          case 'sNpcMove':
            name = data.name;
            sprite = data.sprite;
            x = data.x;
            y = data.y;
            if (name in this.npcs) {
              npc = this.npcs[name];
              npc.setMapPos(x, y);
              npc.x = x;
              npc.y = y;
            } else {
              npc = new Npc(name, sprite);
              npc.setMapPos(x, y);
              npc.x = x;
              npc.y = y;
              this.tileMap.addChild(npc, 1);
              this.npcs[name] = npc;
            }
            break;
          case 'sStartBattle':
            console.log('sStartBattle');
            if (data.name in this.npcs) {
              npc = this.npcs[data.name];
              g.me = this.avatar;
              g.enemy = npc;
              battleScene = new BattleScene;
              transition = cc.TransitionProgressRadialCW.create(0.5, battleScene);
              cc.director.runScene(transition);
            }
            break;
          case 'sQuit':
            if (quit in this.otherPcs) {
              other = this.otherPcs[quit];
              this.tileMap.removeChild(other);
              delete this.otherPcs[quit];
            }
        }
      }
      if (!cc.sys.isNative) {
        if (!this.avatar) {
          return;
        }
        mapSize = this.tileMap.getContentSize();
        _ref = [this.avatar.mX, this.avatar.mY], mX = _ref[0], mY = _ref[1];
        _ref1 = [0, 0], dX = _ref1[0], dY = _ref1[1];
        if (g.keys[cc.KEY.right] && mX < mapSize.width) {
          dX += 3;
        }
        if (g.keys[cc.KEY.left] && mX > 0) {
          dX -= 3;
        }
        if (g.keys[cc.KEY.up] && mY < mapSize.height) {
          dY += 3;
        }
        if (g.keys[cc.KEY.down] && mY > 0) {
          dY -= 3;
        }
        this.avatar.setMapPos(mX + dX, mY + dY);
        if (dX !== 0 || dY !== 0) {
          socket.emit('cMove', {
            x: this.avatar.mX,
            y: this.avatar.mY
          });
        }
        mapPos = this.tileMap.getPosition();
        mapPos.x = size.width / 2 - this.avatar.mX;
        mapPos.y = size.height / 2 - this.avatar.mY;
        return this.tileMap.setPosition(mapPos);
      }
    }
  });

  root.GameScene = cc.Scene.extend({
    onEnter: function() {
      var layer;
      this._super();
      layer = new GameLayer();
      layer.init();
      return this.addChild(layer);
    }
  });

}).call(this);

//# sourceMappingURL=gameScene.map
