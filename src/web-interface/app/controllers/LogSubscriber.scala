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

  /* chatLogType = 1 > 도배 감지
                 = 2 > 욕설

   */

  def receive = {
    // 욕설 및 도배
    case common.ChatLog(chatLogType, id, message) => {
      GameLog.insertLog(1, chatLogType, id, chatLogType match{ case 1 => "도배가 감지되었습니다." case 0 => "욕설을 하고 있습니다.=>"+message})
    }
    // 매크로 로그
    case common.MacroDetection(id,sort,avg,stddev) => {
      GameLog.insertLog(2, sort, id, common.C.MacroType(sort) + " 매크로가 감지되었습니다.")
    }
    case common.MachinePerformance(cpuperf, memperf) => {
    }
  }


}