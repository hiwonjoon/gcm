package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class User(id: String, password: String, level:Int)

object User {

  // -- Parsers

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("member.id") ~
      get[String]("member.pw") ~
      get[Int]("member.level") map {
      case id ~ password ~ level => User(id, password,level)
    }
  }

  // -- Queries

  /**
   * Authenticate a User.
   */
  def authenticate(id: String, password: String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
         SELECT * FROM member WHERE
         id = {id} and pw = {password}
        """
      ).
          on(
          'id -> id,
          'password -> password
        ).as(User.simple.singleOpt)
    }
  }

  /**
   * Create a User.
   */
  def create(user: User): User = {
    DB.
      withConnection { implicit connection =>
      SQL(
        """
          INSERT INTO member VALUES (
            {id}, {pw}, {level}
          )
        """
      ).on(
          'id -> user.id,
          'pw -> user.password,
          'level -> user.level
        ).executeUpdate()

      user

    }
  }
}
