package models.gallery

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.Future
import reactivemongo.bson.BSONDocument
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Created by bdickele on 29/01/14.
 */
object GalleryRW extends Controller with MongoController {

  def collection = db.collection[BSONCollection]("gallery")


  /**
   *
   * @param galleryId
   * @return
   */
  def findById(galleryId: Int): Future[Option[Gallery]] =
    collection.
      find(BSONDocument("galleryId" -> galleryId)).
      one[Gallery]

}
