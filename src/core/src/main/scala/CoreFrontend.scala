package core;

import akka.actor.{Props, ActorSystem, ActorRef, Actor}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress
import spray.json.{DefaultJsonProtocol, RootJsonFormat, JsString, JsNumber, JsObject, JsonParser, JsValue, DeserializationException}
import spray.json.pimpAny
import spray.httpx.SprayJsonSupport._

/**
 * Created by wonjoon-g on 2014. 6. 4..
 */

object JsonProtocol extends DefaultJsonProtocol{
  implicit val userchatFormat = jsonFormat3(UserChat);
}

class PacketHandler extends Actor {
  import Tcp._
  import JsonProtocol._

  val frontend = context.actorOf(Props[CoreFrontend])

  var buf = ByteString()
  def receive = {
    case Received(data) => {
      buf ++= data
      buf = processPacket(buf, (jo) => {
        val parsed = for (msgType <- jo.fields.get("msgType"); body <- jo.fields.get("body")) yield (msgType, body)
        if (parsed.isDefined) {
          try {
            parsed match {
              case Some((JsString("chat"), body)) =>
                frontend !(sender, body.convertTo[UserChat])
              case _ =>
                println("? in CoreFrontend.scala")
            }
          }
          catch {
            case _ => println("Parse Error. Json Object : " + jo.prettyPrint)
          }
        } else {
          println("??? in CoreFrontEnd.scala")
          // handle error
        }
      })
    }
    case PeerClosed => context stop self
  }

  /*

 {
  "msgType": "chat",
  "body": {
    "from": "John",
    "to": "Jane",
    "msg": "Hello, World wtf"
  }
}
   */

  def parseJson(buf: ByteString, index: Int): Option[(JsObject, Int)] = {
    try {
      val nowBuf = buf.drop(index)
      val str = nowBuf.utf8String
      val (jsonSize, jsonStart) = header findFirstMatchIn str match {
        case Some(m) => (m.group(1).toString.toInt, m.end)
        case None => (0, 0)
      }

      if (jsonStart > 0 && nowBuf.length >= jsonStart + jsonSize) {
        val jsonStr = nowBuf.drop(jsonStart).take(jsonSize).utf8String
        val json = JsonParser(jsonStr).asJsObject
        return Some(json, index + jsonStart + jsonSize)
      } else {
        return None
      }
    } catch {
      case _ => return None
    }
  }

  val header = "^(\\d+)\\r?\\n\\r?\\n".r
  def processPacket(buf: ByteString, handler: (JsObject) => Unit): ByteString = {
    var index = 0
    while (true)
    {
      val res = parseJson(buf, index)
      if (!res.isDefined)
        return buf.drop(index)

      handler(res.get._1)
      index = res.get._2
    }

    buf.drop(index)
  }
}

class Listener(port: Int) extends Actor {
  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost",port))

  def receive = {
    case Bound(localAddress) =>
      println("Bound : " + localAddress)
    case Connected(remote, _) =>
      println("Connected: " + remote)
      val handler = context.actorOf(Props[PacketHandler])
      sender ! Register(handler)
    case CommandFailed(_: Bind) =>
      println("Bind Error")
      context stop self
  }
}


class CoreFrontend extends Actor {

  var backends = IndexedSeq.empty[ActorRef];

  def receive = {
    case (handler:ActorRef,action:UserChat) =>
      //TODO : 지금 그대로 게임 서버로 다시 보내주고 있음. 이 부분 필터링이랑, 도배 쪽 해가지고 쑥덕쑥덕 고치면 됨.
      println("Message : " + action.msg )
      import JsonProtocol._

      val json = JsObject(
        "msgType" -> JsString("chat"),
        "body" -> action.toJson
      )
      val body = ByteString(json.prettyPrint)
      val header = ByteString(s"${body.length}\r\n\r\n")
      handler ! Tcp.Write(header ++ body)
    case _ =>
      println("?? in Core Frontend")
  }
}