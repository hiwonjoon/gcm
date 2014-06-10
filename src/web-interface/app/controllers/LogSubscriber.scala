package controllers

/**
 * Created by wonjoon-g on 2014. 5. 1..
 */

import akka.actor.Actor
import play.api._
import models._
import common._

class LogSubscriber extends Actor {

  def receive = {
    case "Hello" => {
      Logger.info("Hello from Core!");
    }
    case common.ChatLog(id, message) => {
      Logger.info(id + " " + message)
      GameLog.insertLog(1, id, message)
    }
  }


}