package models

import models._
import controllers.Global
import controllers._
import anorm._
import anorm.SqlParser._
import scala.concurrent.Future
import play.api.libs.json._

import java.text.SimpleDateFormat
import play.api.Logger

//logType : 1  => 도배
//logType : 2  => 어뷰징

case class GameLog(logType : String, logSubType:String, username : String, contents : String, date: String)

object GameLog extends ((String, String, String, String, String) => GameLog) {

  implicit val jsonFormat = Json.format[GameLog]

  val gameLogs =
    int("type") ~
      int("subtype")~
      str("username") ~
      str("contents") ~
      str("date") map {
      case     logType~logSubType~username~contents~datetime =>
        GameLog(getLogTypeName(logType),getLogSubTypeName(logType,logSubType),username,contents,datetime)
    }

  def getLogTypeName(logType: Int) : String = logType match {
      case 1 => "채팅로그"
      case 2 => "FraudDetect"
  }

  def getLogSubTypeName(logType : Int, logSubType: Int) : String = logType match {
    case 1 => { common.C.ChatLogType(logSubType)}
    case 2 => { common.C.MacroType(logSubType)}
  }

  def getByLogType(logType:Int, logSubType:Int, startIndex:Int, count:Int, username:Option[String]) = scala.concurrent.Future {
    DB.withConnection { implicit connection =>
      val totalCountResult = SQL(
        """
          SELECT COUNT(1) count
          FROM logs
          where type={logType}
        """
      ).on('logType -> logType).apply()

      var totalCount = totalCountResult.head[Long]("count")
      var FilteredCount = totalCount;
      username match {
        case Some("") | None => { // username 이 없다.
          logSubType match {
            case -1 => {  // username 도 없고 subType도 없는 경우
              val gameloglist = SQL(
                """
              SELECT
                type,
                subtype,
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
              (totalCount, FilteredCount, gameloglist)
            }
            case _ => {  // username 은 없는데 subType은 있다.
              val FilteredResult = SQL(
                """
              SELECT COUNT(1) count
              FROM logs
              where type={logType}
               and subtype={logSubType}
                """
              ).on('logType -> logType,
                  'logSubType -> logSubType).apply()
              FilteredCount = FilteredResult.head[Long]("count")

              val gameloglist = SQL(
                """
              SELECT
                type,
                subtype,
                username,
                contents,
                date
              FROM logs
              WHERE type={logType}
              AND subtype={logSubType}
              ORDER BY date desc
              LIMIT {startIndex},{count}
                """
              ).on(
                  'logType -> logType,
                  'logSubType -> logSubType,
                  'startIndex -> startIndex,
                  'count -> count
                ).as(gameLogs *)
              (totalCount, FilteredCount, gameloglist)
            }
          }
        }
        case Some(username) => {
          logSubType match {
            case -1 => {// username 은 있는데 subType도 없는 경우
              val FilteredResult = SQL(
                """
              SELECT COUNT(1) count
              FROM logs
              where type={logType}
               and username like {username}
                """
              ).on('logType -> logType,
                  'username-> ('%'+username+'%')).apply()
              FilteredCount = FilteredResult.head[Long]("count")

              val gameloglist = SQL(
                """
                SELECT
                  type,
                  subtype,
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
                  'username -> ("%" + username +"%")
                ).as(gameLogs *)
              (totalCount, FilteredCount, gameloglist)
            }
            case _ => {// username 은 있는데 subType도 있는 경우
              val FilteredResult = SQL(
                """
              SELECT COUNT(1) count
              FROM logs
              where type={logType}
               and subtype={logSubType}
               and username like {username}
                """
              ).on('logType -> logType,
                   'logSubType -> logSubType,
                  'username-> ('%'+username+'%')).apply()
              FilteredCount = FilteredResult.head[Long]("count")

              val gameloglist = SQL(
                """
                SELECT
                  type,
                  subtype,
                  username,
                  contents,
                  date
                FROM logs
                WHERE type={logType}
                  AND subtype={logSubType}
                  AND username like {username}
                ORDER BY date desc
                LIMIT {startIndex},{count}
                """
              ).on(
                  'logType -> logType,
                  'startIndex -> startIndex,
                  'logSubType -> logSubType,
                  'count -> count,
                  'username -> ("%" + username +"%")
                ).as(gameLogs *)
              (totalCount, FilteredCount, gameloglist)
            }
          }

        }
      }

    }
  }
  def getByLogType(logType:Int, startIndex:Int, count:Int, username:Option[String]) = scala.concurrent.Future {
    DB.withConnection { implicit connection =>
      username match {
        case Some("") | None => {
          SQL(
            """
        SELECT
          type,
          subtype,
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
          subtype,
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

  def insertLog(logType:Int, logSubType:Int, username:String , message:String) = scala.concurrent.Future {

    DB.withConnection { implicit connection =>

      if(logType == 1) {
        /*
        val result = SQL(
          """
          SELECT COUNT(1) count
          FROM logs
          where type={logType}
            and username={username}
            and date > datetime('now', 'localtime', '-5 minute')
          """
        ).on('logType -> logType,
            'username-> username,
            'contents ->( "" +  +"%")).apply()
        val count = result.head[Long]("count")
        Logger.info("count : " + count)
        if(count == 0)*/
        {
          SQL(
            """
          INSERT INTO logs (
            type,
            subtype,
            username,
            contents,
            date
          ) VALUES (
            {type},
            {subtype},
            {username},
            {contents},
            datetime(CURRENT_TIMESTAMP,'localtime')
          )
            """
          ).on(
              'type -> logType,
              'subtype -> logSubType,
              'username -> username,
              'contents -> message
            ).executeInsert()
        }
      }
      else
      {
        SQL(
          """
          INSERT INTO logs (
            type,
            subtype,
            username,
            contents,
            date
          ) VALUES (
            {type},
            {subtype},
            {username},
            {contents},
            strftime('%Y-%m-%d %H:%M',datetime(CURRENT_TIMESTAMP,'localtime'))
          );
          """
        ).on(
            'type -> logType,
            'subtype -> logSubType,
            'username -> username,
            'contents -> message
          ).executeInsert()
      }
    }
  }
}
