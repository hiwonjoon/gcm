import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress
import spray.json.{DefaultJsonProtocol, JsString, JsonParser, JsObject}

case class Chat(from: String, to: String, msg: String)

object JsonProtocol extends DefaultJsonProtocol {
  implicit val chatFormat = jsonFormat3(Chat)
}

class PacketHandler extends Actor {
  import Tcp._
  import JsonProtocol._

  var buf = ByteString()
  def receive = {
    case Received(data) => {
      buf ++= data
      buf = processPacket(buf, (jo) => {
        val parsed = for (msgType <- jo.fields.get("msgType"); body <- jo.fields.get("body")) yield (msgType, body)
        if (parsed.isDefined) {
          parsed match {
            case Some((JsString("chat"), body)) => onChat(sender(), body.convertTo[Chat])
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
    val filtered = chat.msg.replaceAll("wtf", "???")
    val json = JsObject(
      "msgType" -> JsString("chat"),
      "body" -> JsObject(
        "from" -> JsString(chat.from),
        "to" -> JsString(chat.to),
        "msg" -> JsString(filtered)
      )
    )
    sender ! Write(ByteString(json.prettyPrint))
  }

  val header = "^(\\d+)\\r?\\n\\r?\\n".r
  def processPacket(buf: ByteString, handler: (JsObject) => Unit): ByteString = {
    var index = 0

    while (true)
    {
      val str = buf.drop(index).utf8String
      val (jsonSize, jsonStart) = header findFirstMatchIn str match {
        case Some(m) => (m.group(1).toString.toInt, m.end)
        case None => (0, 0)
      }

      if (jsonStart > 0 && str.length >= jsonStart + jsonSize) {
        val jsonStr = str.substring(jsonStart, jsonStart + jsonSize)

        val json = JsonParser(jsonStr).asJsObject
        handler(json)

        index += jsonStart + jsonSize
      } else {
        return buf.drop(index)
      }
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
      sender() ! Register(handler)
  }
}


object Main extends App
{
  val port = 1338
  ActorSystem().actorOf(Props(new ChatFilter(port)))
}