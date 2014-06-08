// Generated by CoffeeScript 1.7.1
(function() {
  var GameServer, Npc, Player, Projectile, World, fs, net, path;

  fs = require('fs');

  path = require('path');

  net = require('net');

  Player = (function() {
    function Player(world, socket, name, sprite) {
      this.world = world;
      this.socket = socket;
      this.name = name;
      this.sprite = sprite;
    }

    Player.prototype.x = 0;

    Player.prototype.y = 0;

    Player.prototype.occupied = false;

    Player.prototype.move = function(x, y) {};

    Player.prototype.attack = function(x, y) {};

    Player.prototype.tick = function() {};

    return Player;

  })();

  Npc = (function() {
    function Npc(world, name, sprite) {
      this.world = world;
      this.name = name;
      this.sprite = sprite;
    }

    Npc.prototype.x = 0;

    Npc.prototype.y = 0;

    Npc.prototype.occupied = false;

    Npc.prototype.testTick = 0;

    Npc.prototype.move = function(x, y) {};

    Npc.prototype.attack = function(x, y) {};

    Npc.prototype.tick = function() {
      var x, y;
      this.testTick++;
      if (this.testTick % 10 === 0) {
        x = this.x - 2 + Math.floor(Math.random() * 5);
        y = this.y - 2 + Math.floor(Math.random() * 5);
        return this.world.processNpcMove(this, x, y);
      }
    };

    return Npc;

  })();

  Projectile = (function() {
    function Projectile(world, name, sprite) {
      this.world = world;
      this.name = name;
      this.sprite = sprite;
    }

    Projectile.prototype.x = 0;

    Projectile.prototype.y = 0;

    Projectile.prototype.tick = function() {};

    return Projectile;

  })();

  World = (function() {
    function World() {}

    World.prototype.playerIdTable = {};

    World.prototype.playerNameTable = {};

    World.prototype.npcTable = {};

    World.prototype.projectileTable = {};

    World.prototype.npcSpriteList = [];

    World.prototype.maxNpc = 30;

    World.prototype.mapWidth = 1200;

    World.prototype.mapHeight = 900;

    World.prototype.index = 0;

    World.prototype.processLogin = function(socket) {
      var k, player, playerSocket, v, _ref, _ref1, _results;
      player = this.playerIdTable[socket.id];
      if (!player) {
        return;
      }
      playerSocket = player.socket;
      playerSocket.emit('sLogin', {
        name: player.name,
        sprite: player.sprite
      });
      _ref = this.playerNameTable;
      for (k in _ref) {
        v = _ref[k];
        if (v.name !== player.name) {
          playerSocket.emit('sPcMove', {
            name: v.name,
            sprite: v.sprite,
            x: v.x,
            y: v.y
          });
        }
      }
      _ref1 = this.npcTable;
      _results = [];
      for (k in _ref1) {
        v = _ref1[k];
        _results.push(playerSocket.emit('sNpcMove', {
          name: v.name,
          sprite: v.sprite,
          x: v.x,
          y: v.y
        }));
      }
      return _results;
    };

    World.prototype.processLogout = function(socket) {
      var player, playerSocket;
      player = this.playerIdTable[socket.id];
      if (!player) {
        return;
      }
      playerSocket = player.socket;
      playerSocket.broadcast.emit('sQuit', player.name);
      delete this.playerIdTable[playerSocket.id];
      return delete this.playerNameTable[player.name];
    };

    World.prototype.processPcMove = function(socket, x, y) {
      var player, playerSocket;
      player = this.playerIdTable[socket.id];
      if (!player) {
        return;
      }
      playerSocket = player.socket;
      player.x = x;
      player.y = y;
      return playerSocket.broadcast.emit('sPcMove', {
        name: player.name,
        sprite: player.sprite,
        x: player.x,
        y: player.y
      });
    };

    World.prototype.processBattle = function(socket, x, y) {
      var k, player, playerSocket, v, _ref, _results;
      player = this.playerIdTable[socket.id];
      if (!player) {
        return;
      }
      playerSocket = player.socket;
      player.x = x;
      player.y = y;
      _ref = this.npcTable;
      _results = [];
      for (k in _ref) {
        v = _ref[k];
        if ((Math.abs(x - v.x) < 5 && Math.abs(y - v.y) < 5) && v.occupied === false) {
          player.occupied = true;
          v.occupied = true;
          playerSocket.emit('sStartBattle', {
            name: v.name,
            sprite: v.sprite
          });
          break;
        } else {
          _results.push(void 0);
        }
      }
      return _results;
    };

    World.prototype.processNpcMove = function(npc, x, y) {
      var k, v, _ref, _results;
      npc.x = x;
      npc.y = y;
      _ref = this.playerNameTable;
      _results = [];
      for (k in _ref) {
        v = _ref[k];
        _results.push(v.socket.emit('sNpcMove', {
          name: npc.name,
          sprite: npc.sprite,
          x: npc.x,
          y: npc.y
        }));
      }
      return _results;
    };

    World.prototype.processChat = function(from, to, msg) {
      var player, playerSocket;
      player = this.playerNameTable[from];
      if (!player) {
        return;
      }
      playerSocket = player.socket;
      playerSocket.emit('sChat', 'You said: ' + msg);
      return playerSocket.broadcast.emit('sChat', player.name + ' said: ' + msg);
    };

    World.prototype.spawnNpc = function() {
      var k, npc, rand, v, _ref, _results;
      rand = Math.floor(Math.random() * this.npcSpriteList.length);
      npc = new Npc(this, "" + this.index + "_test", this.npcSpriteList[rand]);
      npc.x = Math.floor(Math.random() * this.mapWidth);
      npc.y = Math.floor(Math.random() * this.mapHeight);
      this.npcTable[npc.name] = npc;
      this.index++;
      _ref = this.playerNameTable;
      _results = [];
      for (k in _ref) {
        v = _ref[k];
        _results.push(v.socket.emit('sNpcMove', {
          name: npc.name,
          sprite: npc.sprite,
          x: npc.x,
          y: npc.y
        }));
      }
      return _results;
    };

    World.prototype.tick = function() {
      var k, nowNpc, v, _ref, _ref1, _ref2;
      _ref = this.projectileTable;
      for (k in _ref) {
        v = _ref[k];
        v.tick();
      }
      _ref1 = this.npcTable;
      for (k in _ref1) {
        v = _ref1[k];
        v.tick();
      }
      _ref2 = this.playerIdTable;
      for (k in _ref2) {
        v = _ref2[k];
        v.tick();
      }
      nowNpc = (function() {
        var _ref3, _results;
        _ref3 = this.npcTable;
        _results = [];
        for (k in _ref3) {
          v = _ref3[k];
          _results.push(v);
        }
        return _results;
      }).call(this);
      if (nowNpc.length < this.maxNpc) {
        return this.spawnNpc();
      }
    };

    World.prototype.loadDatasheet = function() {
      var dataDir, file, fileList, loadFromCsv, npcTemplateCsv, npcTemplateTable, skillTemplateTable, skillTeplateCsv, spawnTemplateCsv, spawnTemplateTable;
      dataDir = path.join(__dirname, 'data');
      skillTeplateCsv = path.join(dataDir, 'skill.csv');
      npcTemplateCsv = path.join(dataDir, 'npc.csv');
      spawnTemplateCsv = path.join(dataDir, 'spawn.csv');
      loadFromCsv = function(csvPath, onLoadRecord) {
        var line, lines, text, _i, _len, _results;
        text = fs.readFileSync(csvPath, 'utf8');
        lines = text.split('\n');
        _results = [];
        for (_i = 0, _len = lines.length; _i < _len; _i++) {
          line = lines[_i];
          if (line.trim().length > 0) {
            _results.push(onLoadRecord(line.split(',')));
          }
        }
        return _results;
      };
      skillTemplateTable = {};
      loadFromCsv(skillTeplateCsv, function(record) {
        var atk, coolTime, id, name;
        id = record[0], name = record[1], atk = record[2], coolTime = record[3];
        id = parseInt(id);
        atk = parseInt(atk);
        coolTime = parseInt(coolTime);
        return skillTemplateTable[id] = new SkillTemplate(id, name, atk, coolTime);
      });
      npcTemplateTable = {};
      loadFromCsv(npcTemplateCsv, function(record) {
        var hp, id, name, skill, skillList, skillListStr, skillTemplateId, sprite, _i, _len, _ref;
        id = record[0], name = record[1], sprite = record[2], hp = record[3], skillListStr = record[4];
        id = parseInt(id);
        hp = parseInt(hp);
        skillList = [];
        _ref = skillListStr.split(';');
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
          skillTemplateId = _ref[_i];
          skill = skillTemplateTable[skillTemplateId];
          if (skill) {
            skillList.push(skill);
          }
        }
        return npcTemplateTable[id] = new NpcTemplate(id, name, sprite, hp, skillList);
      });
      spawnTemplateTable = {};
      loadFromCsv(spawnTemplateCsv, function(record) {
        var id, npcTemplate, npcTemplateId, respawnMsec, x, y;
        id = record[0], npcTemplateId = record[1], x = record[2], y = record[3], respawnMsec = record[4];
        id = parseInt(id);
        npcTemplateId = parseInt(npcTemplateId);
        x = parseInt(x);
        y = parseInt(y);
        respawnMsec = parseInt(respawnMsec);
        npcTemplate = npcTemplateTable[npcTemplateId];
        if (npcTemplate) {
          return spawnTemplateTable[id] = new SpawnTemplate(id, npcTemplate, x, y, respawnMsec);
        }
      });
      fileList = fs.readdirSync(path.join(__dirname, 'public', 'res', 'npc'));
      return this.npcSpriteList = (function() {
        var _i, _len, _results;
        _results = [];
        for (_i = 0, _len = fileList.length; _i < _len; _i++) {
          file = fileList[_i];
          if (/\.png$/.test(file)) {
            _results.push(file.substring(0, file.length - 4));
          }
        }
        return _results;
      })();
    };

    World.prototype.init = function() {
      this.loadDatasheet();
      return setInterval((function(_this) {
        return function() {
          return _this.tick();
        };
      })(this), 100);
    };

    World.prototype.createPlayer = function(socket, name, sprite) {
      var player;
      player = new Player(this, socket, name, sprite);
      this.playerIdTable[socket.id] = player;
      this.playerNameTable[player.name] = player;
      return this.processLogin(socket);
    };

    return World;

  })();

  GameServer = (function() {
    var makePacket;

    function GameServer(httpServer) {
      this.httpServer = httpServer;
    }

    GameServer.prototype.init = function() {
      this.handleConnection();
      this.connectToGcm('127.0.0.1', 1338);
      this.world = new World(this);
      return this.world.init();
    };

    GameServer.prototype.getPlayerBySocket = function(socket) {
      return this.world.playerIdTable[socket.id];
    };

    GameServer.prototype.handleConnection = function() {
      var io;
      io = require('socket.io').listen(this.httpServer);
      return io.on('connection', (function(_this) {
        return function(socket) {
          console.log(socket.id);

          /*
              i = Math.floor Math.random() * maleNames.length
              j = Math.floor Math.random() * sprites.length
              myName = maleNames[i]
              mySprite = sprites[j]
              playerTable[socket.id] = new Player myName, mySprite
              maleNames.splice i, 1
              socket.emit 'sConnection', { name: myName, sprite: mySprite }
           */
          socket.on('cMove', function(data) {
            return _this.world.processPcMove(socket, data.x, data.y);
          });
          socket.on('cStartBattle', function(data) {
            return _this.world.processBattle(socket, data.x, data.y);
          });
          socket.on('cAttack', function(data) {
            return _this.world.processAttack(socket, data.x, data.y);
          });
          socket.on('cLogin', function(data) {
            return _this.world.createPlayer(socket, data.name, data.sprite);
          });
          socket.on('cChat', function(data) {
            if (_this.gcmClient) {
              return _this.sendToGcm({
                msgType: 'chat',
                body: {
                  from: data.from,
                  to: data.to,
                  msg: data.msg
                }
              });
            } else {
              return _this.world.processChat(data.from, data.to, data.msg);
            }
          });
          return socket.on('disconnect', function() {
            return _this.world.processLogout(socket);
          });
        };
      })(this));
    };

    makePacket = function(jsonData) {
      var body, header, packet;
      body = new Buffer(JSON.stringify(jsonData), 'utf8');
      header = new Buffer("" + body.length + "\r\n\r\n", 'utf8');
      packet = new Buffer(header.length + body.length);
      header.copy(packet);
      body.copy(packet, header.length);
      return packet;
    };

    GameServer.prototype.sendToGcm = function(jsonData) {
      return this.gcmClient.write(makePacket(jsonData));
    };

    GameServer.prototype.gcmClient = null;

    GameServer.prototype.onGcm = function(json) {
      var data;
      switch (json.msgType) {
        case 'chat':
          data = json.body;
          return this.world.processChat(data.from, data.to, data.msg);
      }
    };

    GameServer.prototype.connectToGcm = function(ip, port) {
      var buf, client;
      client = net.createConnection(port, ip);
      buf = new Buffer(0);
      client.on('error', (function(_this) {
        return function(err) {
          console.log('Error!!! try to reconnect after 10 secs');
          return setTimeout(function() {
            return _this.connectToGcm(ip, port);
          }, 10000);
        };
      })(this));
      client.on('connect', (function(_this) {
        return function() {
          console.log('Connected');
          _this.gcmClient = client;
          return _this.sendToGcm({
            msgType: 'chat',
            body: {
              from: 'John',
              to: 'Jane',
              msg: 'Hello, World wtf2 오호'
            }
          });
        };
      })(this));
      client.on('data', (function(_this) {
        return function(data) {
          var json, jsonBegin, jsonEnd, jsonSize, jsonStr, match, matchSize, newBuf, nextPos, str, _results;
          newBuf = new Buffer(buf.length + data.length);
          buf.copy(newBuf);
          data.copy(newBuf, buf.length);
          buf = newBuf;
          _results = [];
          while (true) {
            str = buf.toString('utf8');
            match = /^(\d+)\r?\n\r?\n/.exec(str);
            if (!match) {
              break;
            }
            matchSize = match[0].length;
            jsonSize = parseInt(match[1]);
            nextPos = match.index + matchSize + jsonSize;
            if (buf.length < nextPos) {
              break;
            }
            jsonBegin = match.index + matchSize;
            jsonEnd = jsonBegin + jsonSize;
            jsonStr = buf.toString('utf8', jsonBegin, jsonEnd);
            json = JSON.parse(jsonStr);
            _this.onGcm(json);
            _results.push(buf = buf.slice(nextPos));
          }
          return _results;
        };
      })(this));
      return client.on('close', (function(_this) {
        return function() {
          return console.log('Connection closed');
        };
      })(this));
    };

    return GameServer;

  })();

  module.exports = function(server) {
    var gameServer;
    gameServer = new GameServer(server);
    return gameServer.init();
  };

}).call(this);

//# sourceMappingURL=gameServer.map
