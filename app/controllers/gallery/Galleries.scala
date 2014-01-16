package controllers.gallery

import play.api.mvc.{AnyContent, Action, Controller}
import scala.concurrent.Future
import models.gallery.GalleryBasic
import play.api.libs.concurrent.Execution.Implicits._

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Galleries extends Controller {

  //TODO calculer l'ID de la derniere categorie
  def view  = TODO

  /*
  def view(categoryId: Int) = Unit {
    //val future: Future[List[GalleryBasic]] = GalleryRW.findAllBasic(categoryId)
    val future: List[GalleryBasic] = GalleryRW.findAllBasic(categoryId)

    future.map {
      galleries => Ok(views.html.gallery.gallery(galleries))
    }.recover {
      case e =>
        e.printStackTrace()
        BadRequest(e.getMessage())
    }
  }
  */

}
