package common

import scala.beans.BeanProperty
import scala.collection.immutable.Map

case class Request(packet:RegisterPacket)
class RegisterPacket extends Serializable {
  var packetType = ""
  var statement = ""
  var eventTypes = Map[String, Class[_ <: Any]]()
}

case class EsperEvent(eventType: String, underlying: AnyRef)

case class Chat(@BeanProperty id: String, @BeanProperty message: String)
case class ChatAbusing(@BeanProperty id: String)

