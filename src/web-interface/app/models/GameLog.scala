package models

import anorm._
import anorm.SqlParser._
import scala.concurrent.Future
import play.api.libs.json._

import models._
import controllers.Global

case class GameLog(logType : Int, username : String, contents : String, date: DateTime)

object GameLog extends ((Int, String, String, DateTime) => GameLog) {

  implicit val jsonFormat = Json.format[GameLog]

  val gameLogs =
    int("type") ~
      str("username") ~
      str("contents") ~
      str("date") map {
      case     logType~username~contents~date =>
        GameLog(logType,username,contents,new DateTime(date))
    }

  def getByLogType(logType:Int) = scala.concurrent.Future {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT
            type,
            username,
            contents,
            date
          FROM logs

        """
      ).on(

        ).as(gameLogs *)
    }
  }

  def getAllLog() = scala.concurrent.Future {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT
            type,
            username,
            contents,
            date
          FROM logs;
        """
      ).on().as(gameLogs.singleOpt)
    }
  }

  def insertLog(logType:Int, username:String , message:String) = scala.concurrent.Future {

    DB.withConnection { implicit connection =>
      SQL(
        """
          INSERT INTO logs (
            type,
            username,
            contents,
            date
          ) VALUES (
            {type},
            {username},
            {contents},
            datetime(CURRENT_TIMESTAMP,'localtime')
          );
        """
      ).on(
          'type -> logType,
          'username -> username,
          'contents -> message
        ).executeInsert()
    }
  }
}
