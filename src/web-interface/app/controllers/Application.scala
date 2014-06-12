package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._
import views._


object Application extends Controller {

  val loginForm = Form(
    tuple(
      "id" -> text,
      "password" -> text
    ) verifying ("Invalid id or password", result => result match {
      case (id, password) => User.authenticate(id, password).isDefined
    })
  )

  def authenticate = Action { implicit request =>
     loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.Application.index).withSession("id" -> user._1)
     )
  }
  def logout = Action {
    Redirect(routes.Application.login).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }


  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def menuJs(menu: String) = Action { implicit request =>
    Ok(views.js.menu(menu))
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }
}

trait Secured {
  /**
   * Retrieve the connected user id.
   */
  private def username(request: RequestHeader) = request.session.get("id")
  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)

  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(this.username, onUnauthorized)
  { user =>
    Action(request => f(user)(request))
  }
}
















