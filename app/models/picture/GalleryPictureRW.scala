package models.picture

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.Future
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.{Match, Unwind, Aggregate}
import play.api.libs.concurrent.Execution.Implicits._


/**
 * Created by bdickele on 29/01/14.
 */
object GalleryPictureRW extends Controller with MongoController {

  def collection = db.collection[BSONCollection]("gallery")


  /**
   *
   * @param galleryId
   * @return
   */
  def findByGalleryId(galleryId: Int): Future[List[GalleryPicture]] = {
    val command = Aggregate("gallery", Seq(
      Match(BSONDocument("galleryId" -> galleryId)),
      Unwind("pictures")))

    val future: Future[Stream[BSONDocument]] = db.command(command)

    future.map(stream =>
      stream.toList.map(GalleryPicture.GalleryPictureBSONHandler.read(_)))
  }
}

