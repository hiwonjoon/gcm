package common

import scala.beans.BeanProperty
import scala.collection.immutable.Map
import scala.collection.mutable.ArrayBuffer

case class Request(packet:RegisterPacket)
class RegisterPacket extends Serializable {
  var packetType = ""
  var statement = ArrayBuffer[String] ()
  var eventTypes = Map[String, Class[_ <: Any]]()
}

case class EsperEvent(eventType: String, underlying: AnyRef)

case class Chat(@BeanProperty id: String, @BeanProperty message: String)
case class ChatAbusing(@BeanProperty id: String)
case class ChatSlang(@BeanProperty id: String)

object C {
  val filterStr:Array[String] = Array("시발", "좆", "ㅅㅂ", "지랄", "새끼", "개새끼", "샹놈")
}
