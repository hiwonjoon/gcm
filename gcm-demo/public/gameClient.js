// Generated by CoffeeScript 1.7.1
(function() {
  var addMessage, emitSuper, inputElement, onSuper, root, socket;

  root = typeof exports !== "undefined" && exports !== null ? exports : this;

  root.g = {
    score: 0,
    keys: {
      39: false,
      37: false,
      38: false,
      40: false
    },
    logged: false,
    name: null,
    sprite: null,
    packets: [],
    me: null,
    enemy: null,
    enemyIsNpc: false,
    firstAttack: false
  };

  addMessage = function(from, msg) {
    var container, elem, first, labelClass;
    container = $('#messages');
    first = container.children(':first');
    if (!from) {
      labelClass = 'label-danger';
    } else if (from === g.name) {
      labelClass = 'label-primary';
    } else {
      labelClass = 'label-success';
    }
    elem = $("<div><span class='label " + labelClass + "'>" + from + "</span> <span class='chat-text'>" + msg + "</span></div>");
    if (first.size() === 0) {
      return container.append(elem);
    } else {
      return elem.insertBefore(first);
    }
  };

  socket = io.connect(window.location.origin);

  root.socket = socket;

  emitSuper = socket.emit;

  socket.emit = function() {
    return emitSuper.apply(socket, arguments);
  };

  onSuper = socket.$emit;

  socket.$emit = function() {
    var _ref;
    if ((_ref = arguments[0]) === 'sChat' || _ref === 'sLogin') {
      return onSuper.apply(socket, arguments);
    } else {
      return root.g.packets.push({
        msgType: arguments[0],
        data: arguments[1]
      });
    }
  };

  socket.on('sChat', function(content) {
    return addMessage(content.from, content.msg);
  });

  socket.on('sLogin', function(content) {
    var _ref;
    console.log(content);
    _ref = [content.name, content.sprite], g.name = _ref[0], g.sprite = _ref[1];
    return cc.game.run();
  });

  inputElement = document.getElementById('input');

  inputElement.onkeydown = function(keyboardEvent) {
    if (keyboardEvent.keyCode === 13) {
      socket.emit('cChat', {
        from: g.name,
        to: '',
        msg: inputElement.value
      });
      inputElement.value = '';
      return false;
    } else {
      return true;
    }
  };

}).call(this);

//# sourceMappingURL=gameClient.map
