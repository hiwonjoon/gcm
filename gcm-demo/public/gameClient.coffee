root = exports ? this

root.g =
  score: 0
  keys:
    39: false
    37: false
    38: false
    40: false
  logged: false
  name: null
  sprite: null
  packets: []
  me: null
  enemy: null
  enemyIsNpc: false
  firstAttack: false

addMessage = (from, msg) ->
  container = $ '#messages'
  first = container.children ':first'

  unless from
    labelClass = 'label-danger'
  else if from == g.name
    labelClass = 'label-primary'
  else
    labelClass = 'label-success'
  elem = $ "<div><span class='label #{labelClass}'>#{from}</span> <span class='chat-text'>#{msg}</span></div>"
  if first.size() == 0
    container.append elem
  else
    elem.insertBefore first

socket = io.connect window.location.origin
root.socket = socket

emitSuper = socket.emit
socket.emit = ->
  emitSuper.apply socket, arguments

onSuper = socket.$emit
socket.$emit = ->
  if arguments[0] in ['sChat', 'sLogin'] # bypass only 'sChat'
    onSuper.apply socket, arguments
  else
    root.g.packets.push
      msgType: arguments[0]
      data: arguments[1]

socket.on 'sChat', (content) ->
  addMessage content.from, content.msg

socket.on 'sLogin', (content) ->
  console.log content
  [g.name, g.sprite] = [content.name, content.sprite]
  cc.game.run()

inputElement = document.getElementById 'input'
inputElement.onkeydown = (keyboardEvent) ->
  if (keyboardEvent.keyCode == 13)
    socket.emit 'cChat',
      from: g.name
      to: ''
      msg: inputElement.value
    inputElement.value = ''
    false
  else
    true



