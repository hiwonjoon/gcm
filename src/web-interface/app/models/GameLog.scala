package models

import models._
import controllers.Global
import controllers._
import anorm._
import anorm.SqlParser._
import scala.concurrent.Future
import play.api.libs.json._

import java.text.SimpleDateFormat

//logType : 1  => 도배
//logType : 2  => 어뷰징

case class GameLog(logType : String, username : String, contents : String, date: String)

object GameLog extends ((String, String, String, String) => GameLog) {

  implicit val jsonFormat = Json.format[GameLog]

  val gameLogs =
    int("type") ~
      str("username") ~
      str("contents") ~
      str("date") map {
      case     logType~username~contents~datetime =>
        GameLog(getLogTypeName(logType),username,contents,datetime)
    }

  def getLogTypeName(logType: Int) : String = logType match {
      case 1 => "도배"
      case 2 => "매크로"
      case _ => "오류"
  }

  def getByLogType(logType:Int, startIndex:Int, count:Int, username:Option[String]) = scala.concurrent.Future {
    DB.withConnection { implicit connection =>
      username match {
        case Some("") | None => {
          SQL(
            """
        SELECT
          type,
          username,
          contents,
          date
        FROM logs
        WHERE type={logType}
        ORDER BY date desc
        LIMIT {startIndex},{count}
            """
          ).on(
              'logType -> logType,
              'startIndex -> startIndex,
              'count -> count
            ).as(gameLogs *)
        }
        case Some(s) =>{
          SQL(
            """
        SELECT
          type,
          username,
          contents,
          date
        FROM logs
        WHERE type={logType}
          AND username like {username}
        ORDER BY date desc
        LIMIT {startIndex},{count}
            """
          ).on(
              'logType -> logType,
              'startIndex -> startIndex,
              'count -> count,
              'username -> ("%" + s +"%")
            ).as(gameLogs *)
        }
      }
     }
  }

  def countById(logType : Int, username : String) = Future {
    DB.withConnection { implicit connection =>
      val result = SQL(
        """
          SELECT COUNT(1) count
          FROM logs
          where type={logType}
            and username like {username}
        """
      ).on('logType -> logType,
           'username-> ('%'+username+'%')).apply()
      result.head[Long]("count")
    }
  }

  def countAll(logType : Int) = Future {
    DB.withConnection { implicit connection =>
      val result = SQL(
        """
          SELECT COUNT(1) count
          FROM logs
          where type={logType}
        """
      ).on('logType -> logType).apply()

      result.head[Long]("count")
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
