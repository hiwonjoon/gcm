// Generated by CoffeeScript 1.6.3
(function() {
  var Bbs, Bot, Character, IdGenerator, Npc, Pc, World, gcmPerf, net, sys,
    __hasProp = {}.hasOwnProperty,
    __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; };

  net = require('net');

  sys = require('sys');

  Bbs = (function() {
    function Bbs() {}

    Bbs.generateId = function() {
      return this.count++;
    };

    Bbs.count = 0;

    return Bbs;

  })();

  IdGenerator = (function() {
    function IdGenerator() {}

    IdGenerator.generateId = function(prefix) {
      return "" + prefix + "_" + (this.count++);
    };

    IdGenerator.count = 0;

    return IdGenerator;

  })();

  Character = (function() {
    function Character(world, prefix) {
      var size;
      this.world = world;
      size = this.world.size;
      this.x = Math.floor(Math.random() * size.width);
      this.y = Math.floor(Math.random() * size.height);
      this.id = IdGenerator.generateId(prefix);
    }

    Character.prototype.isBound = false;

    Character.prototype.id = null;

    Character.prototype.isNpc = false;

    Character.prototype.x = 0;

    Character.prototype.y = 0;

    Character.prototype.tick = function(now) {};

    return Character;

  })();

  Npc = (function(_super) {
    __extends(Npc, _super);

    function Npc(world) {
      Npc.__super__.constructor.call(this, world, 'NPC');
    }

    Npc.prototype.tick = function(now) {};

    Npc.prototype.isNpc = true;

    return Npc;

  })(Character);

  Pc = (function(_super) {
    __extends(Pc, _super);

    function Pc(world) {
      this.latActionTime = (new Date).getTime() + Math.random() * 300;
      Pc.__super__.constructor.call(this, world, 'PC');
    }

    Pc.prototype.isNpc = false;

    Pc.prototype.opponent = null;

    Pc.prototype.attackNpc = function() {
      var npc, _i, _len, _ref, _results;
      _ref = this.world.npcs;
      _results = [];
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        npc = _ref[_i];
        if (!(!npc.isBound)) {
          continue;
        }
        this.opponent = npc;
        this.opponent.isBound = true;
        this.isBound = true;
        _results.push(this.world.sendBattleResult(false, 1000, this.x, this.y, this, npc, 100, 10));
      }
      return _results;
    };

    Pc.prototype.attackPc = function() {
      var pc, _i, _len, _ref, _results;
      _ref = this.world.pcs;
      _results = [];
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        pc = _ref[_i];
        if (!(!pc.isBound)) {
          continue;
        }
        this.opponent = pc;
        this.opponent.isBound = true;
        this.isBound = true;
        _results.push(this.world.sendBattleResult(false, 1000, this.x, this.y, this, pc, 100, 10));
      }
      return _results;
    };

    Pc.prototype.chat = function() {
      return this.world.sendChat(this.id, '', 'Hello, World');
    };

    Pc.prototype.whisper = function() {
      var pc, _i, _len, _ref, _results;
      _ref = this.world.pcs;
      _results = [];
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        pc = _ref[_i];
        if (!(!pc.isBound)) {
          continue;
        }
        this.opponent = pc;
        this.opponent.isBound = true;
        this.isBound = true;
        _results.push(this.world.sendChat(this.id, pc.id, 'How are You?'));
      }
      return _results;
    };

    Pc.prototype.write = function() {
      return this.world.sendBbs(Bbs.generateId(), this.id, 'Title', 'Contents');
    };

    Pc.prototype.wander = function() {
      var newX, newY;
      newX = this.x + Math.floor(Math.random() * 4) - 2;
      newY = this.y + Math.floor(Math.random() * 4) - 2;
      if (newX < 0 || newX > this.world.width) {
        return;
      }
      if (newY < 0 || newY > this.world.height) {
        return;
      }
      this.world.sendMove(this.id, this.x, this.y, newX, newY);
      this.x = newX;
      return this.y = newY;
    };

    Pc.prototype.lastActionTime = 0;

    Pc.prototype.tick = function(now) {
      var rand;
      if ((now - this.lastActionTime) < 1000) {
        return;
      }
      this.lastActionTime = now;
      this.isBound = false;
      if (this.opponent) {
        this.opponent.isBound = false;
        this.oponent = null;
      }
      rand = Math.floor(Math.random() * 10);
      switch (rand) {
        case 0:
          return this.attackNpc();
        case 1:
          return this.attackPc();
        case 2:
          return this.chat();
        case 3:
          return this.whisper();
        case 4:
          return this.write();
        case 5:
          return this.wander();
      }
    };

    return Pc;

  })(Character);

  Bot = (function(_super) {
    __extends(Bot, _super);

    function Bot(world) {
      Bot.__super__.constructor.call(this, world);
    }

    Bot.prototype.lastActionTime = 0;

    Bot.prototype.tick = function(now) {
      if (now - this.lastActionTime < 1000) {
        return;
      }
      return this.attackNpc();
    };

    return Bot;

  })(Pc);

  World = (function() {
    function World() {}

    World.prototype.size = {
      width: 800,
      height: 800
    };

    World.prototype.npcs = [];

    World.prototype.pcs = [];

    World.prototype.bots = [];

    World.prototype.start = function() {
      this.connectToGcm('127.0.0.1', 1338);
      return this.startTick();
    };

    World.prototype.startTick = function() {
      return this.tick();
    };

    World.prototype.tick = function() {
      var bot, now, npc, pc, _i, _j, _k, _len, _len1, _len2, _ref, _ref1, _ref2,
        _this = this;
      now = (new Date).getTime();
      _ref = this.pcs;
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        pc = _ref[_i];
        pc.tick(now);
      }
      _ref1 = this.bots;
      for (_j = 0, _len1 = _ref1.length; _j < _len1; _j++) {
        bot = _ref1[_j];
        bot.tick(now);
      }
      _ref2 = this.npcs;
      for (_k = 0, _len2 = _ref2.length; _k < _len2; _k++) {
        npc = _ref2[_k];
        npc.tick(now);
      }
      return setTimeout(function() {
        return _this.tick();
      }, 30);
    };

    World.prototype.gcmClient = null;

    World.prototype.init = function(cfg) {
      var bot, i, npc, pc, _i, _j, _k, _ref, _ref1, _ref2, _results;
      this.size.width = cfg.worldWidth;
      this.size.height = cfg.worldHeight;
      this.npcs = [];
      for (i = _i = 0, _ref = cfg.pc; 0 <= _ref ? _i < _ref : _i > _ref; i = 0 <= _ref ? ++_i : --_i) {
        pc = new Pc(this);
        this.pcs.push(pc);
      }
      for (i = _j = 0, _ref1 = cfg.npc; 0 <= _ref1 ? _j < _ref1 : _j > _ref1; i = 0 <= _ref1 ? ++_j : --_j) {
        npc = new Npc(this);
        this.npcs.push(npc);
      }
      _results = [];
      for (i = _k = 0, _ref2 = cfg.bot; 0 <= _ref2 ? _k < _ref2 : _k > _ref2; i = 0 <= _ref2 ? ++_k : --_k) {
        bot = new Bot(this);
        _results.push(this.bots.push(bot));
      }
      return _results;
    };

    World.prototype.makePacket = function(jsonData) {
      var body, header, packet;
      body = new Buffer(JSON.stringify(jsonData), 'utf8');
      header = new Buffer("" + body.length + "\r\n\r\n", 'utf8');
      packet = new Buffer(header.length + body.length);
      header.copy(packet);
      body.copy(packet, header.length);
      return packet;
    };

    World.prototype.sendChat = function(from, to, msg) {
      return this.sendToGcm({
        msgType: 'chat',
        body: {
          from: from,
          to: to,
          msg: msg
        }
      });
    };

    World.prototype.sendBbs = function(id, name, title, txt) {
      return this.sendToGcm({
        msgType: 'bbs',
        body: {
          id: id,
          name: name,
          title: title,
          txt: txt
        }
      });
    };

    World.prototype.sendMove = function(id, srcX, srcY, destX, destY) {
      return this.sendToGcm({
        msgType: 'move',
        body: {
          id: id,
          src: {
            x: srcX,
            y: srcY
          },
          dest: {
            x: destX,
            y: destY
          }
        }
      });
    };

    World.prototype.sendBattleResult = function(isDraw, duration, posX, posY, winner, loser, exp, gold) {
      return this.sendToGcm({
        msgType: 'battleResult',
        body: {
          isDraw: isDraw,
          duration: duration,
          pos: {
            x: posX,
            y: posY
          },
          winner: {
            id: winner.id,
            isNpc: winner.isNpc
          },
          loser: {
            id: loser.id,
            isNpc: loser.isNpc
          },
          reward: {
            exp: exp,
            gold: gold
          }
        }
      });
    };

    World.prototype.sendToGcm = function(jsonData) {
      if (this.gcmClient) {
        this.gcmClient.write(this.makePacket(jsonData));
      }
      return console.log(jsonData);
    };

    World.prototype.onGcm = function(json) {
      var data;
      switch (json.msgType) {
        case 'chat':
          data = json.body;
          return console.log([data.from, data.to, data.msg]);
      }
    };

    World.prototype.connectToGcm = function(ip, port) {
      var buf, client,
        _this = this;
      client = net.createConnection(port, ip);
      buf = new Buffer(0);
      client.on('error', function(err) {
        console.log('Error!!! try to reconnect after 10 secs');
        return setTimeout(function() {
          return _this.connectToGcm(ip, port);
        }, 10000);
      });
      client.on('connect', function() {
        console.log('Connected');
        return _this.gcmClient = client;
      });
      client.on('data', function(data) {
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
      });
      return client.on('close', function() {
        return console.log('Connection closed');
      });
    };

    return World;

  })();

  gcmPerf = new World;

  gcmPerf.init({
    pc: 100,
    bot: 10,
    npc: 1000,
    worldWidth: 1600,
    worldHeight: 1600
  });

  gcmPerf.start();

}).call(this);

/*
//@ sourceMappingURL=app.map
*/