package controllers

import models.GameLog
import play.api._
import play.api.mvc._
import play.api.libs.json._
import anorm._
import anorm.SqlParser._
import scala.concurrent.Future

import play.api.Play.current
import models._
case class GameLogTable(draw : Int, recordsTotal:Long , recordsFiltered:Long, data:List[GameLog])

object GameLogTable extends ((Int, Long, Long, List[GameLog]) => GameLogTable) {
  implicit val jsonFormat = Json.format[GameLogTable]
}

object GameLogs extends Controller {

  def index(logType : Int) = Action {
    if(logType==1)
    {
      Ok(views.html.logView(logType,common.C.ChatLogType))
    }
    else if(logType == 2)
    {
      Ok(views.html.logView(logType,common.C.MacroType))
    }
    else
    {
      Ok("error")
    }

  }

  def getAjax() = Action.async {implicit request =>
    val logType = request.getQueryString("logType") match {case Some(s) => s.toInt case None => 0}
    val draw = request.getQueryString("draw") match { case Some(s) => s.toInt case None => 0 }
    val start = request.getQueryString("start") match {case Some(s) => s.toInt case None => 0 }
    val length = request.getQueryString("length") match {case Some(s) => s.toInt case None => 0}
    val keyword = request.getQueryString("search[value]")
    val subType = request.getQueryString("columns[1][search][value]") match {case Some(s) => s match {case "" => -1 case str => str.toInt} case None=> -1}
    Logger.info(subType + " subType gggg")
    GameLog.getByLogType(logType,subType,start,length,keyword) map {
      case (count, filteredCount, gameLogs:List[GameLog]) => Ok(Json.toJson(GameLogTable(draw,count,filteredCount,gameLogs)))
    }
  }

}
