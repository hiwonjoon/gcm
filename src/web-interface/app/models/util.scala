import anorm._
import anorm.SqlParser._

package object models {

  type Date = java.util.Date

  def DB = play.api.db.DB

  implicit def current = play.api.Play.current

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  type DateTime = org.joda.time.DateTime

}