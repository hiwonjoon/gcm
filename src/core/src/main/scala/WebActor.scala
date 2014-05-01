package core

import akka.actor.{Actor,ActorRef}
import akka.event.Logging
/**
 * Created by wonjoon-g on 2014. 5. 1..
 */
class WebActor extends Actor{
  val log = Logging(context.system,this)
  override def receive = {
    case "Hello" => {
      log.info("Hello from Web!")
      sender ! "Hello"
    }
    case "Bye" => {
      log.info("Web subscriber leaving...ㅠㅠ")
    }
  }
}
