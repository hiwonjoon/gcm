package esper_akka

import akka.actor.{ActorRef, Actor}
import scala.collection.mutable.ArrayBuffer
import common._

object EsperActor {

  var ongoingType = ArrayBuffer.empty[String]

  case object StartProcessing

  case class AttachProcessor(name:String)

  // register the given class with the given name with the esper engine
  case class RegisterEventType(name:String, clz: Class[_ <: Any])

  // not all statements will require a listener, they could simply be inserting into other streams
  case class DeployStatement(epl:String, listener: Option[ActorRef])

  case class DeployModule(text: String, listeners: Map[String,ActorRef])
}

class EsperActor extends Actor with EsperEngine with EsperModule {
  import esper_akka.EsperActor._

  override def receive = initializing

  private def initializing:Receive = {
    case Request(packet) => {
      val isRegistered = ongoingType.contains(packet.packetType)
      if(isRegistered == false) {
        ongoingType += packet.packetType

        packet.eventTypes.foreach {
          case (key, value) => self ! RegisterEventType(key, value)
        }
      }

      packet.statement.foreach {
        str : String => self ! DeployStatement(str, Some(sender))
      }

      self ! StartProcessing
    }

    case RegisterEventType(name, clz) => esperConfig.addEventType(name, clz.getName)
    case DeployStatement(epl, listener) => createEPL(epl)(evt => listener map ( l => l ! evt))
    case DeployModule(text, listeners) => installModule(text) { evt => listeners.get(evt.eventType) map (_ ! evt)}
    case StartProcessing => context.become(dispatchingToEsper)
  }

  private def dispatchingToEsper():Receive = {
    case evt@_ => epRuntime.sendEvent(evt)
  }

}

