package controllers

/**
 * Created by wonjoon-g on 2014. 5. 1..
 */

import akka.actor.Actor
import play.api._
import models._
import common._

case class SendToCore(arg:AnyRef)

class CoreSubscriber extends Actor {
  val remote = context.actorSelection("akka.tcp://core@127.0.0.1:5151/user/WebActor")

  def receive = {
    case SendToCore(arg) => {
      remote ! arg
    }
    case "Hello" => {
      Logger.info("Hello from Core!");
    }
    case "Start" => {
      Logger.info("Core Subscriber Started. Send Hello to remote!")
      remote ! "Hello"
    }
    case "End" => {
      Logger.info("Core Subscriber Ended. Send Hello to remote!")
      remote ! "Bye"
    }
    case common.ForbiddenWords(words) => {
      remote ! common.ForbiddenWords(words)
    }
    case _ => {
      Logger.info("Core Subscriber _!!!")
    }
  }
}