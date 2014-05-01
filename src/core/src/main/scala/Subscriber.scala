package core

import akka.actor.Actor
import common._

class Subscriber extends Actor {

  var remote = context.actorSelection("akka.tcp://akka-esper@127.0.0.1:5150/user/EsperActor")

  def receive = {
    case EsperEvent(_, ChatAbusing(id)) => println(s"채팅창 도배가 감지되었습니다. ($id)")
    case EsperEvent(_, ChatSlang(id)) => println(s"채팅창 비속어가 감지되었습니다. ($id)")

    case "RequestChatDetection" => {
      var packet = new RegisterPacket
      packet.packetType = "Chat"
      packet.statement += s"""
                          insert into ChatAbusing
                          select c.id
                          from Chat.win:time(1 sec) as c
                          having count(*) > 3
                         """

      packet.statement += s"""
                          insert into ChatSlang
                          select c.id
                          from Chat as c
                          where Filtering(c.message)
                          """

      packet.eventTypes += "Chat" -> classOf[Chat]
      packet.eventTypes += "ChatAbusing" -> classOf[ChatAbusing]
      packet.eventTypes += "ChatSlang" -> classOf[ChatSlang]

      remote ! Request(packet)
    }

    case "RegisterSuccess" => {
      remote ! "StartProcessing"
    }

    case Chat(id,msg) => {
      remote ! Chat(id, msg)
    }
  }
}