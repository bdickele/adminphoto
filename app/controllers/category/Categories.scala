package controllers.category

import play.api.mvc.{SimpleResult, Action, Controller}
import models.category.{Category, CategoryRW}
import play.api.Logger
import play.api.cache.Cache
import play.api.Play.current
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Categories extends Controller {

  val CacheCategory = "CacheCategory"


  def view() = Action {
    Ok(views.html.category.category(findAllFromCacheOrDB()))
  }

  // Is it a good practice ? I don't know to this day, but I need that as that list of categories
  // is required in many places of the application
  def findAllFromCacheOrDB(): List[Category] =
    Cache.getOrElse[List[Category]](CacheCategory) {
      Await.result(CategoryRW.findAll, Duration(5, TimeUnit.SECONDS))
    }

  def refresh() = Action {
    clearCache()
    Redirect(routes.Categories.view())
  }

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
    val categories: List[Category] = findAllFromCacheOrDB()

    // Let's retrieve our category
    categories.find(_.categoryId == categoryId) match {
      case Some(category) => {
        val categoryRank = category.rank

        // List is sorted by rank: we reverse it and pick up the first category whose rank is > category's rank
        categories.reverse.find(_.rank > categoryRank) match {
          case None => // nothing to do then
          case Some(otherCategory) => {
            clearCache()
            CategoryRW.update(category.copy(rank = (categoryRank + 1)))
            CategoryRW.update(otherCategory.copy(rank = categoryRank))
          }
        }

        Redirect(routes.Categories.view())
      }
      case None => couldNotFindCategory(categoryId)
    }
  }

  /**
   * A category has to "go down" in the hierarchy of categories
   * @param categoryId ID of category to promote
   * @return
   */
  def down(categoryId: Int) = Action {
    val categories: List[Category] = findAllFromCacheOrDB()

    // Let's retrieve our category
    categories.find(_.categoryId == categoryId) match {
      case Some(category) => {
        val categoryRank = category.rank

        // List is sorted by rank, thus we pick up the first category whose rank is < category's rank
        categories.find(_.rank < categoryRank) match {
          case None => // nothing to do then
          case Some(otherCategory) => {
            clearCache()
            CategoryRW.update(category.copy(rank = (categoryRank - 1)))
            CategoryRW.update(otherCategory.copy(rank = categoryRank))
          }
        }

        Redirect(routes.Categories.view())
      }
      case None => couldNotFindCategory(categoryId)
    }
  }

  def onOffLine(categoryId: Int) = Action {
    findAllFromCacheOrDB().find(_.categoryId == categoryId) match {
      case Some(category) => {
        clearCache()
        CategoryRW.update(category.copy(online = !category.online))
        Redirect(routes.Categories.view())
      }
      case None => couldNotFindCategory(categoryId)
    }
  }

  def couldNotFindCategory(categoryId: Int): SimpleResult = {
    val message = "Could not find category with ID " + categoryId
    Logger.error(message)
    BadRequest(message)
  }
}
