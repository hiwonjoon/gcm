import akka.actor.{ActorSystem, Props}
import scala.util.control.Breaks._

import common._

object Main extends App {

  var system = ActorSystem("akka-processor")
  var subscriber = system.actorOf(Props(classOf[Subscriber]), name = "Subscriber")

  subscriber ! "RequestChatDetection"

  subscriber ! "StartProcessing"


  breakable {
    while (true) {
      var input = readLine()
      input match {
        case "quit" => break
        case str =>
          subscriber ! Chat("overload", str)
      }
    }
  }
}
