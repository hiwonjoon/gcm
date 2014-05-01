package controllers

import play.api._
import play.api.mvc._

object TwitterKeyword extends Controller {

  def index = Action {
    Ok(views.html.twitterKeyword())
    //Ok(views.html.index2())
  }
}
