package controllers.gallery

import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import models.category.Category
import models.gallery.{GalleryBasic, GalleryBasicRW}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import controllers.category.Categories

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Galleries extends Controller {

  def view(passedCategoryId: Int = -1) = Action.async {
    val categories: List[Category] = Categories.findAllFromCacheOrDB()
    val categoryId = if (passedCategoryId > 0) passedCategoryId else categories.head.categoryId

    val future: Future[List[GalleryBasic]] = GalleryBasicRW.findAll(categoryId)

    future.map {
      galleries => Ok(views.html.gallery.gallery(categoryId, categories, galleries))
    }.recover {
      case e =>
        Logger.error(e.getMessage)
        BadRequest(e.getMessage)
    }
  }

  def refresh(categoryId: Int) = Action {
    Redirect(routes.Galleries.view(categoryId))
  }

}
