package controllers.gallery

import play.api.mvc.{SimpleResult, Action, Controller}
import scala.concurrent.{Await, Future}
import models.category.Category
import models.gallery.{Gallery, GalleryRW}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import controllers.category.Categories
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import reactivemongo.bson.{BSONBoolean, BSONInteger}

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Galleries extends Controller {

  def view(passedCategoryId: Int = -1) = Action.async {
    val categories: List[Category] = Categories.findAllFromCacheOrDB()
    val categoryId = if (passedCategoryId > 0) passedCategoryId else categories.head.categoryId

    // In case URL contains an incorrect gallery ID
    categories.exists(c => c.categoryId == categoryId) match {
      case false => Future.successful(Categories.couldNotFindCategory(categoryId))

      case true => GalleryRW.findAll(categoryId).map {
        galleries => Ok(views.html.gallery.gallery(categoryId, categories, galleries))
      }.recover {
        case e =>
          Logger.error(e.getMessage)
          BadRequest(views.html.badRequest(e.getMessage))
      }
    }
  }

  def refresh(categoryId: Int) = Action {
    Redirect(routes.Galleries.view(categoryId))
  }

  /** A gallery has to "go up" in the hierarchy of galleries */
  def up(categoryId: Int, galleryId: Int) = Action {
    val galleries = findAll(categoryId)

    // Let's retrieve our category
    galleries.find(_.galleryId == galleryId) match {
      case Some(gallery) =>
        val galleryRank = gallery.rank

        // List is sorted by rank: we reverse it and pick up the first category whose rank is > category's rank
        galleries.reverse.find(_.rank > galleryRank) match {
          case Some(otherGallery) =>
            GalleryRW.updateField(galleryId, "rank", BSONInteger(galleryRank + 1))
            GalleryRW.updateField(otherGallery.galleryId, "rank", BSONInteger(galleryRank))
          case _ => // nothing to do then
        }

        Redirect(routes.Galleries.view(categoryId))

      case None => couldNotFindGallery(galleryId)
    }
  }

  /** A gallery has to "go down" in the hierarchy of galleries */
  def down(categoryId: Int, galleryId: Int) = Action {
    val galleries = findAll(categoryId)

    // Let's retrieve our category
    galleries.find(_.galleryId == galleryId) match {
      case Some(gallery) =>
        val galleryRank = gallery.rank

        // List is sorted by rank, thus we pick up the first gallery whose rank is < gallery's rank
        galleries.find(_.rank < galleryRank) match {
          case None => // nothing to do then
          case Some(otherGallery) =>
            GalleryRW.updateField(galleryId, "rank", BSONInteger(galleryRank - 1))
            GalleryRW.updateField(otherGallery.galleryId, "rank", BSONInteger(galleryRank))
        }

        Redirect(routes.Galleries.view(categoryId))

      case None => couldNotFindGallery(galleryId)
    }
  }

  def onOffLine(categoryId: Int, galleryId: Int) = Action.async {
    GalleryRW.findById(galleryId).map {
      _ match {
        case Some(gallery) =>
          GalleryRW.updateField(galleryId, "online", BSONBoolean(!gallery.online))
          Redirect(routes.Galleries.view(categoryId))
        case None =>
          couldNotFindGallery(galleryId)
      }
    }
  }

  def findAll(categoryId: Int): List[Gallery] =
    Await.result(GalleryRW.findAll(categoryId), Duration(5, TimeUnit.SECONDS))

  def couldNotFindGallery(galleryId: Int): SimpleResult =
    BadRequest(views.html.badRequest("Could not find a gallery with ID " + galleryId))
}
