package controllers.category

import play.api.mvc.{Action, Controller}
import models.category.CategoryRW

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Categories extends Controller {

  /*
  def view = Action.async {
    val futureCategories: Future[List[Category]] = CategoryRW.loadAll

    futureCategories.map {
      categories => Ok(views.html.category.category(categories))
    }.recover {
      case e =>
        e.printStackTrace()
        BadRequest(e.getMessage())
    }
  }
  */

  def view = Action {
    Ok(views.html.category.category(CategoryRW.loadAll))
  }
}
