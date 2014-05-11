package controllers.category


import play.api.mvc.{SimpleResult, Controller}
import play.api.cache.Cache
import play.api.Play.current
import scala.concurrent.Await
import scala.concurrent.duration._
import language.postfixOps
import securesocial.core.SecureSocial
import service.CategoryService
import play.api.libs.json.{JsObject, Json}
import models.{Role, Category}
import models.WithRole
import models.WithRole
import play.api.libs.json.JsObject
import scala.Some
import play.api.mvc.SimpleResult
import java.util.concurrent.TimeUnit

/**
 * Controller for screen related to list of categories
 * bdickele
 */
object Categories extends Controller with SecureSocial {

  val CacheCategory = "CacheCategory"


  def categories() = SecuredAction { implicit request =>
    clearCache()
    val categories = findAllFromCacheOrDB()

    // Data related to number of galleries per category
    val mapCategoryNbGallery = CategoryService.findNumberOfGalleryByCategory()
    categories.foreach(category => category.nbGallery = mapCategoryNbGallery.getOrElse(category.categoryId, 0))

    Ok(views.html.category.category(categories))
  }

  def findAllFromCacheOrDB(): List[Category] =
    Cache.getOrElse[List[Category]](CacheCategory) {
      Await.result(CategoryService.findAll, 5 seconds)
    }

  def refresh() = SecuredAction { implicit request =>
    clearCache()
    Redirect(routes.Categories.categories())
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
  def up(categoryId: Int) = SecuredAction(WithRole(Role.Writer)) { implicit request =>
    val categories: List[Category] = findAllFromCacheOrDB()

    // Let's retrieve our category
    categories.find(_.categoryId == categoryId) match {
      case Some(category) =>
        val categoryRank = category.rank

        // List is sorted by rank: we reverse it and pick up the first category whose rank is > category's rank
        // find method returns an Option, but if it returns None I don't do nothing
        // That's why I directly use map method
        categories.reverse.find(_.rank > categoryRank).map { categoryAbove =>
          clearCache()
          CategoryService.updateField(category.categoryId, "rank", Json.toJson(categoryRank + 1))
          CategoryService.updateField(categoryAbove.categoryId, "rank", Json.toJson(categoryRank))
        }

        Redirect(routes.Categories.categories())

      case None => couldNotFindCategory(categoryId)
    }
  }

  /**
   * A category has to "go down" in the hierarchy of categories
   * @param categoryId ID of category to promote
   * @return
   */
  def down(categoryId: Int) = SecuredAction(WithRole(Role.Writer)) { implicit request =>
    val categories: List[Category] = findAllFromCacheOrDB()

    // Let's retrieve our category
    categories.find(_.categoryId == categoryId) match {
      case Some(category) =>
        val categoryRank = category.rank

        // List is sorted by rank, thus we pick up the first category whose rank is < category's rank
        categories.find(_.rank < categoryRank).map { categoryUnderneath =>
          clearCache()
          CategoryService.updateField(category.categoryId, "rank", Json.toJson(categoryRank - 1))
          CategoryService.updateField(categoryUnderneath.categoryId, "rank", Json.toJson(categoryRank))
        }

        Redirect(routes.Categories.categories())

      case None => couldNotFindCategory(categoryId)
    }
  }

  def onOffLine(categoryId: Int) = SecuredAction(WithRole(Role.Writer)) { implicit request =>
    findAllFromCacheOrDB().find(_.categoryId == categoryId) match {
      case Some(category) =>
        clearCache()
        CategoryService.updateField(category.categoryId, "online", Json.toJson(!category.online))
        Redirect(routes.Categories.categories())

      case None => couldNotFindCategory(categoryId)
    }
  }

  def deleteCategory(categoryId: Int) = SecuredAction(WithRole(Role.Writer)) { implicit request =>
    CategoryService.delete(categoryId)
    Redirect(routes.Categories.categories())
  }

  def couldNotFindCategory(categoryId: Int): SimpleResult =
    BadRequest(views.html.global.badRequest("Could not find a category with ID " + categoryId))
}
