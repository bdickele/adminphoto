package controllers.category


import play.api.mvc.{SimpleResult, Controller}
import play.api.cache.Cache
import play.api.Play.current
import scala.concurrent.Await
import scala.concurrent.duration._
import securesocial.core.SecureSocial
import service.CategoryService
import play.api.libs.json.Json
import models.Category

/**
 * Controller for screen related to list of categories
 * bdickele
 */
object Categories extends Controller with SecureSocial {

  val CacheCategory = "CacheCategory"


  def view() = SecuredAction {
    implicit request =>
      clearCache()
      Ok(views.html.category.category(findAllFromCacheOrDB()))
  }

  def findAllFromCacheOrDB(): List[Category] =
    Cache.getOrElse[List[Category]](CacheCategory) {
      Await.result(CategoryService.findAll, 5 seconds)
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
              CategoryService.updateField(category.categoryId, "rank", Json.toJson(categoryRank + 1))
              CategoryService.updateField(otherCategory.categoryId, "rank", Json.toJson(categoryRank))
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
              CategoryService.updateField(category.categoryId, "rank", Json.toJson(categoryRank - 1))
              CategoryService.updateField(otherCategory.categoryId, "rank", Json.toJson(categoryRank))
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
          CategoryService.updateField(category.categoryId, "online", Json.toJson(!category.online))
          Redirect(routes.Categories.view())

        case None => couldNotFindCategory(categoryId)
      }
  }

  def couldNotFindCategory(categoryId: Int): SimpleResult =
    BadRequest(views.html.badRequest("Could not find a category with ID " + categoryId))
}
