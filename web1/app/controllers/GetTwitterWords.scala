package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json
import play.api.Routes

case class TwitterWords(text: String, weight: Int)

object GetTwitterWords extends Controller {

  implicit val fooWrites = Json.writes[TwitterWords]

  def getMessage = Action {

    /* Message 전송 받으면 됩니다 */
    /* 예시 */
    Ok(Json.toJson(
      List(TwitterWords("purus",1), TwitterWords("dui",1), TwitterWords("vestibulum",2),
      TwitterWords("Aenean", 2), TwitterWords("hello wordl", 3), TwitterWords("Habitant",3),
      TwitterWords("Nam et", 4), TwitterWords("Leo", 4), TwitterWords("Sit", 1),
      TwitterWords("Dolor", 9), TwitterWords("Ipsum", 10), TwitterWords("Lorem", 14))))
  }

  def javascriptRoutes = Action { implicit request =>
    Ok(Routes.javascriptRouter("jsRoutes")(routes.javascript.GetTwitterWords.getMessage)).as(JAVASCRIPT)
  }

}