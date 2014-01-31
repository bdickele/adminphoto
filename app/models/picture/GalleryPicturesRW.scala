package models.picture

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.Future
import reactivemongo.bson.BSONDocument
import play.api.libs.concurrent.Execution.Implicits._


/**
 * Created by bdickele on 29/01/14.
 */
object GalleryPicturesRW extends Controller with MongoController {

  def collection = db.collection[BSONCollection]("gallery")


  // --------------------------------------------------------------
  // FIND
  // --------------------------------------------------------------

  /**
   * @param galleryId
   * @return Pictures of a gallery
   */
  def findByGalleryId(galleryId: Int): Future[Option[GalleryPictures]] =
    collection.
      find(BSONDocument("galleryId" -> galleryId)).
      one[GalleryPictures]

  // --------------------------------------------------------------
  // CREATE
  // --------------------------------------------------------------


  // --------------------------------------------------------------
  // UPDATE
  // --------------------------------------------------------------
}

