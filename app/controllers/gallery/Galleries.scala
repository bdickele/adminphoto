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

  def view(passedCategoryId: Int = -1) = Action.async {
    val categories: List[Category] = CategoryRW.findAll
    val categoryId = if (passedCategoryId > 0) passedCategoryId else categories.head.categoryId

    val future: Future[List[GalleryBasic]] = GalleryBasicRW.findAll(categoryId)

    future.map {
      galleries => Ok(views.html.gallery.gallery(categoryId, categories, galleries))
    }.recover {
      case e =>
        e.printStackTrace()
        BadRequest(e.getMessage())
    }
  }

}
