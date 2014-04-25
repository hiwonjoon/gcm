package controllers

import play.api._
import play.api.mvc._

object TagCloud extends Controller {

  def index = Action {
    Ok(views.html.tagcloud())
    //Ok(views.html.index2())
  }

}