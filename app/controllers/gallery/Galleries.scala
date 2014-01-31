package controllers.gallery

import play.api.mvc.{SimpleResult, Action, Controller}
import scala.concurrent.{Await, Future}
import models.category.Category
import models.gallery.{GalleryBasic, GalleryBasicRW}
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

    val future: Future[List[GalleryBasic]] = GalleryBasicRW.findAll(categoryId)

    future.map {
      galleries => Ok(views.html.gallery.gallery(categoryId, categories, galleries))
    }.recover {
      case e =>
        Logger.error(e.getMessage)
        BadRequest(e.getMessage)
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
      case Some(gallery) => {
        val galleryRank = gallery.rank

        // List is sorted by rank: we reverse it and pick up the first category whose rank is > category's rank
        galleries.reverse.find(_.rank > galleryRank) match {
          case None => // nothing to do then
          case Some(otherGallery) => {
            GalleryBasicRW.updateField(galleryId, "rank", BSONInteger(galleryRank + 1))
            GalleryBasicRW.updateField(otherGallery.galleryId, "rank", BSONInteger(galleryRank))
          }
        }

        Redirect(routes.Galleries.view(categoryId))
      }
      case None => couldNotFindGallery(galleryId)
    }
  }

  /** A gallery has to "go down" in the hierarchy of galleries */
  def down(categoryId: Int, galleryId: Int) = Action {
    val galleries = findAll(categoryId)

    // Let's retrieve our category
    galleries.find(_.galleryId == galleryId) match {
      case Some(gallery) => {
        val galleryRank = gallery.rank

        // List is sorted by rank, thus we pick up the first gallery whose rank is < gallery's rank
        galleries.find(_.rank < galleryRank) match {
          case None => // nothing to do then
          case Some(otherGallery) => {
            GalleryBasicRW.updateField(galleryId, "rank", BSONInteger(galleryRank - 1))
            GalleryBasicRW.updateField(otherGallery.galleryId, "rank", BSONInteger(galleryRank))
          }
        }

        Redirect(routes.Galleries.view(categoryId))
      }
      case None => couldNotFindGallery(galleryId)
    }
  }

  def onOffLine(categoryId: Int, galleryId: Int) = Action {
    val option = Await.result(GalleryBasicRW.findById(galleryId), Duration(5, TimeUnit.SECONDS))

    option match {
      case Some(gallery) => {
        GalleryBasicRW.updateField(galleryId, "online", BSONBoolean(!gallery.online))
        Redirect(routes.Galleries.view(categoryId))
      }
      case None => couldNotFindGallery(galleryId)
    }
  }

  def findAll(categoryId: Int): List[GalleryBasic] =
    Await.result(GalleryBasicRW.findAll(categoryId), Duration(5, TimeUnit.SECONDS))

  def couldNotFindGallery(galleryId: Int): SimpleResult = {
    val message = "Could not find gallery with ID " + galleryId
    Logger.error(message)
    BadRequest(message)
  }
}
