package controllers

import play.api.mvc._
import play.api.Routes

object Application extends Controller {


  def index = Action {
    Redirect(gallery.routes.Galleries.view(-1))
  }

  /**
   * Javacript routes for javascript and coffeescript files
   * @return
   */
  def javascriptRoutes = Action {
    implicit request =>
      Ok(Routes.javascriptRouter("jsRoutes")(
        controllers.gallery.routes.javascript.Galleries.view)).as("text/javascript")
  }
}
