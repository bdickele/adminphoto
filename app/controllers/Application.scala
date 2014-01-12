package controllers

import play.api._
import play.api.mvc._
import controllers.picture.Pictures

object Application extends Controller {


  def index = Action {
    Redirect(picture.routes.Pictures.pictures0)
  }
}