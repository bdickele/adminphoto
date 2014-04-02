package controllers.category

import play.api.mvc.{SimpleResult, Controller}
import models.category.{Category, CategoryRW}
import play.api.cache.Cache
import play.api.Play.current
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import reactivemongo.bson.{BSONInteger, BSONBoolean}
import securesocial.core.SecureSocial

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Categories extends Controller with SecureSocial {

  val CacheCategory = "CacheCategory"


  def view() = SecuredAction {
    implicit request =>
      Ok(views.html.category.category(findAllFromCacheOrDB()))
  }

  // Is it a good practice ? I don't know to this day, but I need that as that list of categories
  // is required in many places of the application
  def findAllFromCacheOrDB(): List[Category] =
    Cache.getOrElse[List[Category]](CacheCategory) {
      Await.result(CategoryRW.findAll, Duration(5, TimeUnit.SECONDS))
    }

  def refresh() = SecuredAction {
    implicit request =>
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
  def up(categoryId: Int) = SecuredAction {
    implicit request =>
      val categories: List[Category] = findAllFromCacheOrDB()

      // Let's retrieve our category
      categories.find(_.categoryId == categoryId) match {
        case Some(category) =>
          val categoryRank = category.rank

          // List is sorted by rank: we reverse it and pick up the first category whose rank is > category's rank
          categories.reverse.find(_.rank > categoryRank) match {
            case None => // nothing to do then
            case Some(otherCategory) =>
              clearCache()
              CategoryRW.updateField(category.categoryId, "rank", BSONInteger(categoryRank + 1))
              CategoryRW.updateField(otherCategory.categoryId, "rank", BSONInteger(categoryRank))
          }

          Redirect(routes.Categories.view())

        case None => couldNotFindCategory(categoryId)
      }
  }

  /**
   * A category has to "go down" in the hierarchy of categories
   * @param categoryId ID of category to promote
   * @return
   */
  def down(categoryId: Int) = SecuredAction {
    implicit request =>
      val categories: List[Category] = findAllFromCacheOrDB()

      // Let's retrieve our category
      categories.find(_.categoryId == categoryId) match {
        case Some(category) =>
          val categoryRank = category.rank

          // List is sorted by rank, thus we pick up the first category whose rank is < category's rank
          categories.find(_.rank < categoryRank) match {
            case None => // nothing to do then
            case Some(otherCategory) =>
              clearCache()
              CategoryRW.updateField(category.categoryId, "rank", BSONInteger(categoryRank - 1))
              CategoryRW.updateField(otherCategory.categoryId, "rank", BSONInteger(categoryRank))
          }

          Redirect(routes.Categories.view())

        case None => couldNotFindCategory(categoryId)
      }
  }

  def onOffLine(categoryId: Int) = SecuredAction {
    implicit request =>
      findAllFromCacheOrDB().find(_.categoryId == categoryId) match {
        case Some(category) =>
          clearCache()
          CategoryRW.updateField(category.categoryId, "online", BSONBoolean(!category.online))
          Redirect(routes.Categories.view())

        case None => couldNotFindCategory(categoryId)
      }
  }

  def couldNotFindCategory(categoryId: Int): SimpleResult =
    BadRequest(views.html.badRequest("Could not find a category with ID " + categoryId))
}
