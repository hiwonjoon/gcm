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
    Ok(views.html.logView(logType))
  }
/*
  def getAjax(logType:Int, draw:Int, start:Int, length:Int) = Action.async {
    GameLog.countAll(logType).flatMap {
      case count: Long => {
        GameLog.getByLogType(logType, start, length) map {
          case gameLogs: List[GameLog] => Ok(Json.toJson(GameLogTable(draw, count, count, gameLogs)))
          case _ => Ok("")
        }
      }
    }
  }
*/
  def getAjax() = Action.async { implicit request =>

    val logType = request.getQueryString("logType") match {case Some(s) => s.toInt case None => 0}
    val draw = request.getQueryString("draw") match { case Some(s) => s.toInt case None => 0 }
    val start = request.getQueryString("start") match {case Some(s) => s.toInt case None => 0 }
    val length = request.getQueryString("length") match {case Some(s) => s.toInt case None => 0}
    val keyword = request.getQueryString("search[value]")
    GameLog.countAll(logType).flatMap {
      case count: Long => {
        keyword match {
          case None | Some("") => {
            GameLog.getByLogType(logType, start, length, keyword) map {
              case gameLogs: List[GameLog] => Ok(Json.toJson(GameLogTable(draw, count, count, gameLogs)))
              case _ => Ok("")
            }
          }
          case Some(str)=> {
             Logger.info("hello world")
            GameLog.countById(logType,str).flatMap {
              case count_filtered : Long => {
                GameLog.getByLogType(logType, start, length, keyword) map {
                  case gameLogs: List[GameLog] => Ok(Json.toJson(GameLogTable(draw,count,count_filtered,gameLogs)))
                }
              }
            }
          }
        }
      }
    }
  }
  /*
  def get(logType:Int, startIndex:Int, count:Int) = Action.async {
      GameLog.getByLogType(logType,startIndex,count) map {
        case gameLogs: List[GameLog] => Ok(views.html.logTableTemplate(gameLogs.iterator))
        case _ => NoContent
      }
  }*/

}
