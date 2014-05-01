package controllers

/**
 * Created by wonjoon-g on 2014. 5. 1..
 */

import akka.actor.Actor
import play.api._
import models._
import common._

class CoreSubscriber extends Actor {
  var remote = context.actorSelection("akka.tcp://core@127.0.0.1:5151/user/WebActor")

  def receive = {
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
    case Talk(name,text) => {
      remote ! Chat(name,text)
    }
  }
}