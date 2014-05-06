package core

import akka.actor.{Actor, ActorRef}
import common._

class Subscriber extends Actor {

  var esper = context.actorSelection("akka.tcp://akka-esper@127.0.0.1:5150/user/EsperActor")

  def receive = {
    case EsperEvent(_, ChatAbusing(id, origin)) => {
      if(origin != null)
        origin.asInstanceOf[ActorRef] ! ChatLog(id, " 님이 도배를 하고 있습니다.")
    }

    case EsperEvent(_, ChatSlang(id, message, origin)) => {
      if(origin != null)
        origin.asInstanceOf[ActorRef] ! ChatLog(id, " 님이 비속어를 사용했습니다.")
    }

    case "RequestChatDetection" => {
      var packet = new RegisterPacket
      packet.packetType = "Chat"
      packet.statement += s"""
                          insert into ChatAbusing
                          select c.id, c.origin
                          from ChatWithAddress.win:time(1 sec) as c
                          group by id
                          having count(*) > 3
                         """

      packet.statement += s"""
                          insert into ChatSlang
                          select c.id, c.message, c.origin
                          from ChatWithAddress as c
                          where Filtering(c.message)
                          """

      packet.eventTypes += "ChatWithAddress" -> classOf[ChatWithAddress]
      packet.eventTypes += "ChatAbusing" -> classOf[ChatAbusing]
      packet.eventTypes += "ChatSlang" -> classOf[ChatSlang]

      esper ! Request(packet)
    }

    case ChatLog(id, msg) => {
      println(s"($id)$msg")
    }

    case ChatWithAddress(id,msg,origin) => {
      esper ! ChatWithAddress(id,msg,self)
    }

    case EsperError(_) => {
      self ! "RequestChatDetection";
    }
  }
}