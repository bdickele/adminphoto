package controllers.gallery

import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import models.category.{Category, CategoryRW}
import models.gallery.{CategoryAndGalleryRW, CategoryAndGallery}
import play.api.libs.concurrent.Execution.Implicits._

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Galleries extends Controller {

  def view = Action.async {
    val categories: List[Category] = CategoryRW.loadAll
    val lastCategory = categories.last


    val futureCategory: Future[Option[CategoryAndGallery]] = CategoryAndGalleryRW.find(lastCategory.categoryId)

    futureCategory.map {
      category => Ok(views.html.gallery.gallery(categories, category.get))
    }.recover {
      case e =>
        e.printStackTrace()
        BadRequest(e.getMessage())
    }
  }

}
