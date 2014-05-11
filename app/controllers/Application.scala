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
    Redirect(gallery.routes.Galleries.galleries(-1))
  }

  def update = Action {
    GalleryWriteService.updateDatabase()
    Redirect(gallery.routes.Galleries.galleries(-1))
  }

  /**
   * Javacript routes for javascript and coffeescript files
   * @return
   */
  def javascriptRoutes = Action {
    implicit request =>
      Ok(Routes.javascriptRouter("jsRoutes")(
        controllers.category.routes.javascript.Categories.deleteCategory,
        controllers.gallery.routes.javascript.Galleries.galleries,
        controllers.gallery.routes.javascript.GalleryPicSelection.pictures,
        controllers.picture.routes.javascript.Pictures.pictures)).as("text/javascript")
  }
}
