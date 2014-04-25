package esper_akka

import akka.actor.{Props, ActorSystem}

object Main extends App {
  val system = ActorSystem("akka-esper")
  val esperActor = system.actorOf(Props(classOf[EsperActor]), name = "EsperActor")

}
