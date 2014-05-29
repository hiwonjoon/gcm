package core

import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import akka.event.Logging
import java.net.InetSocketAddress
import spray.json.{DefaultJsonProtocol, JsString, JsonParser, JsObject}

case class Chat(from: String, to: String, msg: String)

object JsonProtocol extends DefaultJsonProtocol {
  implicit val chatFormat = jsonFormat3(Chat)
}

class PacketHandler extends Actor {
  import Tcp._
  import JsonProtocol._


  var esper = context.actorSelection("akka.tcp://akka-esper@127.0.0.1:5150/user/EsperActor")
  val log = Logging(context.system,this)

  var buf = ByteString()
  def receive = {
    case Received(data) => {
      buf ++= data
      buf = processPacket(buf, (jo) => {
        val parsed = for (msgType <- jo.fields.get("msgType"); body <- jo.fields.get("body")) yield (msgType, body)
        if (parsed.isDefined) {
          parsed match {
            case Some((JsString("chat"), body)) =>
              onChat(sender, body.convertTo[Chat])
            case _ =>
          }
        } else {
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

  def onChat(sender: ActorRef, chat: Chat) = {

    //val filtered = chat.msg.replaceAll("wtf", "???")
    /*
    val json = JsObject(
      "msgType" -> JsString("chat"),
      "body" -> JsObject(
        "from" -> JsString(chat.from),
        "to" -> JsString(chat.to),
        "msg" -> JsString(filtered)
      )
    )

    val body = ByteString(json.prettyPrint)
    val header = ByteString(s"${body.length}\r\n\r\n")
    sender ! Write(header ++ body)
    */
    log.info(chat.from, chat.msg, sender)
    esper ! common.ChatWithAddress(chat.from, chat.msg, sender)
    //sender ! common.Chat(chat.from,chat.msg)

    val json = JsObject(
      "msgType" -> JsString("chat"),
      "body" -> JsObject(
        "from" -> JsString(chat.from),
        "to" -> JsString(chat.to),
        "msg" -> JsString(chat.msg)
      )
    )

    val body = ByteString(json.prettyPrint)
    val header = ByteString(s"${body.length}\r\n\r\n")
    sender ! Write(header ++ body)

  }

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
      case _ : Throwable => return None
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


class ChatFilter(port: Int) extends Actor {
  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", port))

  def receive = {
    case Bound(localAddress) =>
      println("Bound: " + localAddress)

    case Connected(remote, _) =>
      println("Connected: " + remote)

      val handler = context.actorOf(Props[PacketHandler])
      sender ! Register(handler)
  }
}


/*
object Main extends App
{
  val port = 1338
  ActorSystem().actorOf(Props(new ChatFilter(port)))
} */