package models.gallery

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.{Await, Future}
import reactivemongo.bson.{BSONString, BSONValue, BSONDocument, BSONArray}
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands.LastError
import controllers.gallery.{routes, Galleries}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration


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

  def removePicture(galleryId: Int, index: Int): Future[LastError] = {
    val future: Future[Option[BSONDocument]] = collection.
      find(BSONDocument("galleryId" -> galleryId)).
      one[BSONDocument]
    val doc: BSONDocument = Await.result(future, Duration(5, TimeUnit.SECONDS)).get

    val list: List[BSONValue] = doc.getAs[BSONArray]("pictures").get.values.toList
    val listNew = list.take(index) ::: list.drop(index + 1)
    GalleryRW.updateField(galleryId, "pictures", BSONArray(listNew))
  }

  def addPictures(galleryId: Int, pictures: List[GalleryPic]): Future[LastError] = {
    val array = BSONArray(pictures.map(GalleryPics.GalleryPicBSONHandler.write(_)))
    collection.update(BSONDocument("galleryId" -> galleryId),
      BSONDocument("$pushAll" -> BSONDocument("pictures" -> array)))
  }

  def setPictures(galleryId: Int, pictures: List[GalleryPic]): Future[LastError] = {
    val array = BSONArray(pictures.map(GalleryPics.GalleryPicBSONHandler.write(_)))
    GalleryRW.updateField(galleryId, "pictures", array)
  }

  def updateComment(galleryId: Int, index: Int, comment: Option[String]): Future[LastError] = {
    val future: Future[Option[BSONDocument]] = collection.
      find(BSONDocument("galleryId" -> galleryId)).
      one[BSONDocument]
    val doc: BSONDocument = Await.result(future, Duration(5, TimeUnit.SECONDS)).get

    val list: List[BSONValue] = doc.getAs[BSONArray]("pictures").get.values.toList

    val picDoc: BSONDocument = list.apply(index).asInstanceOf[BSONDocument]

    var newDoc: BSONDocument = BSONDocument()
    for ((key, value) <- picDoc.elements.toList) {
         if (key != "comment")
           newDoc = newDoc ++ BSONDocument(key -> value)
    }

    comment match {
      case None => // Nothing to do
      case Some(c) => newDoc = newDoc ++ BSONDocument("comment" -> c)
    }

    val listNew = list.take(index) ::: newDoc :: list.drop(index + 1)
    GalleryRW.updateField(galleryId, "pictures", BSONArray(listNew))
  }
}

