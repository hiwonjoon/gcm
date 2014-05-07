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
  moves: []
  quits: []

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

socket.on 'sLogin', (data) ->
  root.g.sprite = data.sprite
  root.g.name = data.name

socket.on 'sChat', (content) ->
  addMessage content

socket.on 'sMove', (data) ->
  root.g.moves.push data

socket.on 'sQuit', (data) ->
  root.g.quits.push data

inputElement = document.getElementById 'input'
inputElement.onkeydown = (keyboardEvent) ->
  if (keyboardEvent.keyCode == 13)
    socket.emit 'cChat', inputElement.value
    inputElement.value = ''
    false
  else
    true

