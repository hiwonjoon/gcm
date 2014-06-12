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
    // 도배 로그
    case common.ChatLog(id, message) => {
      GameLog.insertLog(1, id, message)
    }
    // 매크로 로그
    case common.MacroDetection(id,sort,avg,stddev) => {
      GameLog.insertLog(2, id, "avg: " + sort + " avg:" + avg + "stddev: " + stddev)
    }
  }


}