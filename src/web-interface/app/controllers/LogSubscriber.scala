package controllers

/**
 * Created by wonjoon-g on 2014. 5. 1..
 */

import akka.actor.Actor
import play.api._
import models._
import common._
import java.util.HashMap._
import java.util.ArrayList._

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
    //
    case common.MachinePerformance(cpuperf, memperf) => {
      var it = cpuperf.iterator()
      while(it.hasNext())
      {
        var a = it.next().entrySet().iterator()
        while(a.hasNext()) {
          var b = a.next()
          Logger.info(b.getKey() + " & " + b.getValue())
        }
      }
      var mem_it = memperf.entrySet().iterator()
      while(mem_it.hasNext()) {
        var c = mem_it.next()
        Logger.info(c.getKey() + " & " + c.getValue())
      }

    }
  }


}