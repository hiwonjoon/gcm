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

object GameLogs extends Controller {

  def get(logType:Int) = Action.async {

    GameLog.getByLogType(logType) map {
      case gameLogs:List[GameLog] => Ok(Json.toJson(gameLogs))
      case _ => NoContent
    }
  }


}
