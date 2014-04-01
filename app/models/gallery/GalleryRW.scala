package models.gallery

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.{Await, Future}
import reactivemongo.bson.{BSONValue, BSONInteger, BSON, BSONDocument}
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands.LastError
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import org.joda.time.YearMonth
import play.api.Logger

/**
 * Created by bdickele on 29/01/14.
 */
object GalleryRW extends Controller with MongoController {

  def collection = db.collection[BSONCollection]("gallery")

  // --------------------------------------------------------------
  // FIND
  // --------------------------------------------------------------

  def findAll(categoryId: Int): Future[List[Gallery]] =
    collection.
      find(BSONDocument("categoryId" -> categoryId)).
      sort(BSONDocument("rank" -> -1)).
      cursor[Gallery].
      collect[List]()

  def findById(galleryId: Int): Future[Option[Gallery]] =
    collection.
      find(BSONDocument("galleryId" -> galleryId)).
      one[Gallery]

  /** As title has to be unique, we need that method */
  def findByTitle(title: String): Future[Option[Gallery]] =
    collection.
      find(BSONDocument("title" -> title)).
      one[Gallery]

  def findMaxGalleryId: Int = {
    val future: Future[Option[BSONDocument]] =
      collection.find(BSONDocument()).
        sort(BSONDocument("galleryId" -> -1)).
        one[BSONDocument]
    val option: Option[BSONDocument] = Await.result(future, Duration(5, TimeUnit.SECONDS))

    option match {
      case None => 0
      case Some(doc) => doc.getAs[BSONInteger]("galleryId").get.value
    }
  }

  def findMaxRankForCategory(categoryId: Int): Int = {
    val future: Future[Option[BSONDocument]] =
      collection.find(BSONDocument("categoryId" -> categoryId)).
        sort(BSONDocument("rank" -> -1)).
        one[BSONDocument]
    val option: Option[BSONDocument] = Await.result(future, Duration(5, TimeUnit.SECONDS))

    option match {
      case None => -1
      case Some(doc) => doc.getAs[BSONInteger]("rank").get.value
    }
  }

  /**
   * Purpose of that method is to returning gallery, of the same category, that is right before a
   * gallery with passed rank. If currentRank is 0 then it will return None
   * @param categoryId Category ID
   * @param currentRank Rank of current gallery in the category
   * @return
   */
  def findPreviousGalleryInCategory(categoryId: Int, currentRank: Int): Future[Option[Gallery]] =
    collection.
      find(BSONDocument("categoryId" -> categoryId, "rank" -> BSONDocument("$lt" -> currentRank))).
      sort(BSONDocument("rank" -> -1)).
      one[Gallery]

  def findLastGalleryOfCategory(categoryId: Int): Future[Option[Gallery]] =
    collection.
      find(BSONDocument("categoryId" -> categoryId)).
      sort(BSONDocument("rank" -> -1)).
      one[Gallery]

  /**
   * Purpose of that method is to returning gallery, of the same category, that is right after a
   * gallery with passed rank. If currentRank has a rank higher than all its siblings, then it will return None
   * @param categoryId Category ID
   * @param currentRank Rank of current gallery in the category
   * @return
   */
  def findNextGalleryInCategory(categoryId: Int, currentRank: Int): Future[Option[Gallery]] =
    collection.
      find(BSONDocument("categoryId" -> categoryId, "rank" -> BSONDocument("$gt" -> currentRank))).
      sort(BSONDocument("rank" -> 1)).
      one[Gallery]

  def findFirstGalleryOfCategory(categoryId: Int): Future[Option[Gallery]] =
    collection.
      find(BSONDocument("categoryId" -> categoryId)).
      sort(BSONDocument("rank" -> 1)).
      one[Gallery]

  // --------------------------------------------------------------
  // CREATE
  // --------------------------------------------------------------

  /** Create a gallery (without thumbnail or picture) */
  def create(categoryId: Int,
             title: String,
             year: Int,
             month: Int,
             comment: String,
             online: Boolean): Future[LastError] = {
    val gallery = Gallery(
      categoryId,
      GalleryRW.findMaxGalleryId + 1,
      findMaxRankForCategory(categoryId) + 1,
      new YearMonth(year, month),
      title,
      if (comment == "") None else Some(comment),
      "",
      0,
      online)

    Logger.info("Gallery to be created : " + gallery)
    collection.insert(BSON.writeDocument(gallery))
  }

  // --------------------------------------------------------------
  // UPDATE
  // --------------------------------------------------------------

  /** In that method we update everything that is not related to pictures */
  def update(galleryId: Int,
             categoryId: Int,
             title: String,
             year: Int,
             month: Int,
             comment: Option[String],
             online: Boolean): Future[LastError] = {
    val selector = BSONDocument("galleryId" -> galleryId)

    val modifier = BSONDocument(
      "$set" -> BSONDocument(
        "categoryId" -> categoryId,
        "title" -> title,
        "date" -> (year + "/" + month),
        "online" -> online),
      comment match {
        case None => "$unset" -> BSONDocument("comment" -> 1)
        case Some(d) => "$set" -> BSONDocument("comment" -> d)
      })

    collection.update(selector, modifier)
  }

  def updateField(galleryId: Int, field: String, value: BSONValue): Future[LastError] =
    collection.update(BSONDocument("galleryId" -> galleryId),
      BSONDocument("$set" -> BSONDocument(field -> value)))
}
