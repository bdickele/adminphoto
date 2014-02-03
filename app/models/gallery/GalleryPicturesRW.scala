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

  /**
   * To update comment of a picture.
   * Je ne suis pas convaincu par ce que j'ai fait : est-ce qu'il y a un moyen de mettre à jour
   * un élémen précis du tableau ?
   * @param galleryId
   * @param index
   * @param comment
   * @return
   */
  def updateComment(galleryId: Int, index: Int, comment: Option[String]): Future[LastError] = {

    def copyDocExceptComment(pairs: List[(String, BSONValue)], docAcc: BSONDocument): BSONDocument = pairs match {
      case Nil => docAcc
      case head :: tail => head match {
        case ("comment", value) => copyDocExceptComment(tail, docAcc)
        case (key, value) => copyDocExceptComment(tail, BSONDocument(key -> value) ++ docAcc)
      }
    }

    val future: Future[Option[BSONDocument]] = collection.
      find(BSONDocument("galleryId" -> galleryId)).
      one[BSONDocument]
    val doc: BSONDocument = Await.result(future, Duration(5, TimeUnit.SECONDS)).get

    val picturesRaw: List[BSONValue] = doc.getAs[BSONArray]("pictures").get.values.toList
    val picDoc: BSONDocument = picturesRaw.apply(index).asInstanceOf[BSONDocument]

    val newDoc = copyDocExceptComment(picDoc.elements.toList, BSONDocument()) ++ (comment match {
      case None => BSONDocument()
      case Some(c) => BSONDocument("comment" -> c)
    })

    val listNew = picturesRaw.take(index) ::: newDoc :: picturesRaw.drop(index + 1)
    GalleryRW.updateField(galleryId, "pictures", BSONArray(listNew))
  }
}

