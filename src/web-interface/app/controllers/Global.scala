package controllers

import play.api._
import akka.actor.{Props,ActorSystem}

object Global extends GlobalSettings {

  val system = ActorSystem("web")
  val core_subscriber = system.actorOf(Props(classOf[CoreSubscriber]));

  override def onStart(app: Application) {
    Logger.info("Hello Started!")

    core_subscriber ! "Start"
  }
  override def onStop(app: Application) {
    Logger.info("Hello Ended!")
    core_subscriber ! "End"
  }
}