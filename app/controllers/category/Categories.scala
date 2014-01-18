package controllers.category

import play.api.mvc.{Action, Controller}
import models.category.CategoryRW

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Categories extends Controller {

  def view = Action {
    Ok(views.html.category.category(CategoryRW.loadAll))
  }
}
