package service

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import scala.concurrent.{Await, Future}
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.duration._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.Some
import models.Gallery
import models.GalleryMapper._


/**
 * Service related to reading of data related to galleries
 * bdickele
 */
object GalleryReadService extends Controller with MongoController {

  def collection = db.collection[JSONCollection]("gallery")


  // --------------------------------------------------------------
  // Find
  // --------------------------------------------------------------

  def findAll(categoryId: Int): Future[List[Gallery]] =
    collection.
      find(Json.obj("categoryId" -> categoryId)).
      sort(Json.obj("rank" -> -1)).
      cursor[Gallery].
      collect[List]()

  def findOne(criteria: JsObject, sorter: JsObject = Json.obj()): Future[Option[Gallery]] =
    collection.
      find(criteria).
      sort(sorter).
      one[Gallery]

  def findById(galleryId: Int): Future[Option[Gallery]] = findOne(Json.obj("galleryId" -> galleryId))

  /** As title has to be unique, we need that method */
  def findByTitle(title: String): Future[Option[Gallery]] = findOne(Json.obj("title" -> title))

  // Returns categoryId of a gallery
  def findCategoryId(galleryId: Int): Int = {
    val future: Future[Option[JsObject]] =
      collection.find(Json.obj()).
        sort(Json.obj("galleryId" -> -1)).
        one[JsObject]

    val option: Option[JsObject] = Await.result(future, 5 seconds)
    option match {
      case None => 0
      case Some(doc) => (doc \ "categoryId").as[Int]
    }
  }

  def findMaxGalleryId: Int = {
    val future: Future[Option[JsObject]] =
      collection.find(Json.obj()).
        sort(Json.obj("galleryId" -> -1)).
        one[JsObject]

    val option: Option[JsObject] = Await.result(future, 5 seconds)
    option match {
      case None => 0
      case Some(doc) => (doc \ "galleryId").as[Int]
    }
  }

  def findMaxRankForCategory(categoryId: Int): Int = {
    val future: Future[Option[JsObject]] =
      collection.find(Json.obj("categoryId" -> categoryId)).
        sort(Json.obj("rank" -> -1)).
        one[JsObject]

    val option: Option[JsObject] = Await.result(future, 5 seconds)
    option match {
      case None => -1
      case Some(doc) => (doc \ "rank").as[Int]
    }
  }

  // --------------------------------------------------------------
  // Find methods for previous/next navigation
  // --------------------------------------------------------------

  /**
   * Purpose of that method is to return gallery, of the same category, that is right before a
   * gallery with passed rank. If currentRank is 0 then it will return None
   * @param categoryId Category ID
   * @param currentRank Rank of current gallery in the category
   * @return
   */
  def findPreviousGalleryInCategory(categoryId: Int, currentRank: Int): Future[Option[Gallery]] =
    findOne(
      Json.obj("categoryId" -> categoryId, "rank" -> Json.obj("$lt" -> currentRank)),
      Json.obj("rank" -> -1))


  def findLastGalleryOfCategory(categoryId: Int): Future[Option[Gallery]] =
    findOne(
      Json.obj("categoryId" -> categoryId),
      Json.obj("rank" -> -1))

  /**
   * Purpose of that method is to return gallery, of the same category, that is right after a
   * gallery with passed rank. If currentRank has a rank higher than all its siblings, then it will return None
   * @param categoryId Category ID
   * @param currentRank Rank of current gallery in the category
   * @return
   */
  def findNextGalleryInCategory(categoryId: Int, currentRank: Int): Future[Option[Gallery]] =
    findOne(
      Json.obj("categoryId" -> categoryId, "rank" -> Json.obj("$gt" -> currentRank)),
      Json.obj("rank" -> 1))

  def findFirstGalleryOfCategory(categoryId: Int): Future[Option[Gallery]] =
    findOne(
      Json.obj("categoryId" -> categoryId),
      Json.obj("rank" -> 1))
}
