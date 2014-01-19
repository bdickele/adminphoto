package controllers.category

import play.api.mvc.{Action, Controller}
import models.category.{Category, CategoryRW}
import play.api.Logger
import play.api.cache.Cache
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import play.api.Play.current
import reactivemongo.bson.BSONDocument

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Categories extends Controller {

  val CacheCategory = "CacheCategory"


  def view() = Action {
    val categories: List[Category] = Cache.getOrElse[List[Category]](CacheCategory) {
      findAll()
    }
    Ok(views.html.category.category(categories))
  }

  // Is it a good practice ? I don't know to this day, but I need that as that list of categories
  // is required in many places of the application
  def findAll(): List[Category] =
    Await.result(CategoryRW.findAll, Duration(5, TimeUnit.SECONDS))

  /** Clear cache from categories */
  def clearCache() =
    Cache.getAs[List[Category]](CacheCategory) match {
      case Some(list) => Cache.remove(CacheCategory)
      case None => // Nothing to do
    }

  /**
   * A category has to "go up" in the hierarchy of categories
   * @param categoryId ID of category to promote
   * @return
   */
  def up(categoryId: Int) = Action {
    val categories: List[Category] = findAll()

    // Let's retrieve our category
    categories.find(_.categoryId == categoryId) match {
      case Some(category) => {
        val categoryRank = category.rank

        // Can we find the category that is just before our category ? List is already sorted by rank,
        // thus we can use find method that picks up the first element matching filter.
        categories.find(_.rank > categoryRank) match {
          case None => // nothing to do then
          case Some(otherCategory) => {

            CategoryRW.update(categoryId,
              BSONDocument("$set" -> BSONDocument("rank" -> (categoryRank + 1))))
            CategoryRW.update(otherCategory.categoryId,
              BSONDocument("$set" -> BSONDocument("rank" -> categoryRank)))

            // Let's clear cache !
            clearCache()
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
