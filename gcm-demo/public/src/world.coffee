root = exports ? this

class CharacterInfo
  constructor: (@name, @sprite, @x, @y) ->

class root.World
  constructor: (@avatarName, @avatarSprite) ->

  otherPcInfos: {}
  npcInfos: {}
  gameLayer: null

  setGameLayer: (layer) ->
    @gameLayer = layer

  handlePacket: (msgType, data) ->
    switch msgType
      when 'sPcMove'
        name = data.name
        sprite = data.sprite
        x = data.x
        y = data.y

        if name of @otherPcInfos
          info = @otherPcInfos[name]
          [info.x, info.y] = [x, y]
          @gameLayer?.onMovePc? name, x, y
        else
          info = new CharacterInfo name, sprite, x, y
          @otherPcInfos[name] = info
          @gameLayer?.onNewPc? name, sprite, x, y

      when 'sNpcMove'
        name = data.name
        sprite = data.sprite
        x = data.x
        y = data.y

        if name of @npcInfos
          info = @npcInfos[name]
          [info.x, info.y] = [x, y]
          @gameLayer?.onMoveNpc? name, x, y
        else
          info = new CharacterInfo name, sprite, x, y
          @npcInfos[name] = info
          @gameLayer?.onNewNpc? name, sprite, x, y

      when 'sQuit'
        name = data
        delete @otherPcInfos[name]
        @gameLayer?.onQuitPc? name
