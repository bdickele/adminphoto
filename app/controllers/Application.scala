package controllers

import play.api.mvc._
import play.api.Routes
import service.GalleryWriteService

/**
 * Default controller
 * bdickele
 */
object Application extends Controller {


  def index = Action {
    Redirect(gallery.routes.Galleries.view(-1))
  }

  def update = Action {
    GalleryWriteService.updateDatabase()
    Redirect(gallery.routes.Galleries.view(-1))
  }

  /**
   * Javacript routes for javascript and coffeescript files
   * @return
   */
  def javascriptRoutes = Action {
    implicit request =>
      Ok(Routes.javascriptRouter("jsRoutes")(
        controllers.gallery.routes.javascript.Galleries.view,
        controllers.gallery.routes.javascript.GalleryPicSelection.view,
        controllers.picture.routes.javascript.Pictures.view)).as("text/javascript")
  }
}
