package controllers.gallery

import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import models.category.{Category, CategoryRW}
import models.gallery.{GalleryBasic, GalleryBasicRW}
import play.api.libs.concurrent.Execution.Implicits._

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Galleries extends Controller {

  def view = Action.async {
    val categories: List[Category] = CategoryRW.loadAll
    val lastCategory = categories.last


    val future: Future[List[GalleryBasic]] = GalleryBasicRW.findAll(lastCategory.categoryId)

    future.map {
      galleries => Ok(views.html.gallery.gallery(categories, galleries))
    }.recover {
      case e =>
        e.printStackTrace()
        BadRequest(e.getMessage())
    }
  }

}
