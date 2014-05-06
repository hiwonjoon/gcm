package esper_akka

import akka.actor.{ActorRef, Actor}
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.LinkedHashSet
import common._
import akka.event.Logging

//TODO : 지금은 Event 등록 없이 Event를 보내는 것이 가능하다. 이거를 어떡할까? 그냥 미리 다 등록시켜 놓을까. 런타임에 하지 말고.
//TODO : Statement(관찰중인 쿼리) 제외 하는 것?

object EsperActor {
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
  val packet_types = new ArrayBuffer[String];
  val event_types_checker = new LinkedHashSet[Any => Boolean];
  val log = Logging(context.system,this)
  var error_receiver:ActorRef = null;

  override def receive:Receive = {
    case Request(packet) => {
        log.info("Enroll Sender & EventTypes & Statement.")
        error_receiver = sender;
        if( packet_types.contains(packet.packetType) == false ) {
          packet_types :+ packet.packetType;

          packet.eventTypes.foreach {
            case (key, value) => {
              event_types_checker += value.isInstance;
              self ! RegisterEventType(key, value)
            }
          }
          packet.statement.foreach {
            str: String => self ! DeployStatement(str, Some(sender))
          }
        }

      }
    case RegisterEventType(name, clz) => esperConfig.addEventType(name, clz.getName)
    case DeployStatement(epl, listener) => createEPL(epl)(evt => listener map ( l => l ! evt))
    case DeployModule(text, listeners) => installModule(text) { evt => listeners.get(evt.eventType) map (_ ! evt)}
    case evt@_ => {
      val type_matched = event_types_checker.takeWhile( f => f(evt) )
      if( type_matched.size != 0 )
        epRuntime.sendEvent(evt)
      else {
        sender ! EsperError("Not registered event");
      }
    }
  }
}

