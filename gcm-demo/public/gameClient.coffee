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

messageElement = document.getElementById 'messages'
lastMessageElement = null

addMessage = (message) ->
  newMessageElement = document.createElement 'div'
  newMessageText = document.createTextNode message
  newMessageElement.appendChild newMessageText
  messageElement.insertBefore newMessageElement, lastMessageElement
  lastMessageElement = newMessageElement

socket = io.connect window.location.origin
root.socket = socket

emitSuper = socket.emit
socket.emit = ->
  emitSuper.apply socket, arguments

onSuper = socket.$emit
socket.$emit = ->
  if arguments[0] == 'sChat' # bypass only 'sChat'
    onSuper.apply socket, arguments
  else
    root.g.packets.push
      msgType: arguments[0]
      data: arguments[1]

socket.on 'sChat', (content) ->
  addMessage content

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

