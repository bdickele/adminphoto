package controllers.category

import play.api.mvc.{Action, Controller}
import models.category.{Category, CategoryRW}
import play.api.Logger

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Categories extends Controller {

  def view() = Action {
    Ok(views.html.category.category(CategoryRW.findAll))
  }

  /**
   * A category has to "go up" in the hierarchy of categories
   * @param categoryId
   * @return
   */
  def up(categoryId: Int) = Action {
    val categories: List[Category] = CategoryRW.findAll

    // Let's retrieve our category
    categories.find(_.categoryId == categoryId) match {
      case Some(category) => {
        val categoryRank = category.rank

        // Can we find the category that is just before our category ? List is already sorted by rank,
        // thus we can use find method that picks up the first element matching filter.
        categories.find(_.rank > categoryRank) match {
          case None => // nothing to do then
          case Some(otherCategory) => {
            val newCategoryUp = category.copy(rank = categoryRank + 1)
            val newCategoryDown = otherCategory.copy(rank = categoryRank)

            //TODO Demander la mise a jour des categories
          }
        }

        Redirect(routes.Categories.view())
      }
      case None => {
        val message = "Could not find category with ID " + categoryId
        Logger.error(message)
        BadRequest(message)
      }
    }


  }
}
