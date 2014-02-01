package models.gallery

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.Future
import reactivemongo.bson.{BSONArray, BSONDocument}
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands.LastError


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
  def findByGalleryId(galleryId: Int): Future[Option[GalleryPics]] =
    collection.
      find(BSONDocument("galleryId" -> galleryId)).
      one[GalleryPics]


  // --------------------------------------------------------------
  // UPDATE
  // --------------------------------------------------------------

  def updatePictures(galleryId: Int, pictures: List[GalleryPic]): Future[LastError] = {
    val array = BSONArray(pictures.map(GalleryPics.GalleryPicBSONHandler.write(_)))
    GalleryRW.updateField(galleryId, "pictures", array)
  }
}

