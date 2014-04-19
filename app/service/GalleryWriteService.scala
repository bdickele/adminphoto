package service

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import scala.concurrent.{Await, Future}
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands.LastError
import play.api.Logger
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.Some
import models.{Versioning, GalleryPic, Gallery}
import service.mapper.GalleryMapper._
import service.mapper.VersioningMapper._
import scala.concurrent.duration._
import org.joda.time.DateTime

/**
 * Creation or update of gallery or pictures
 * Created by bdickele
 */
object GalleryWriteService extends Controller with MongoController {

  def collection = db.collection[JSONCollection]("gallery")


  // --------------------------------------------------------------
  // Methods related to gallery but not its pictures
  // --------------------------------------------------------------

  /** Create a gallery (without thumbnail or picture) */
  def create(categoryId: Int,
             galleryId: Int,
             title: String,
             comment: String,
             online: Boolean,
             authId: String): Future[LastError] = {
    val gallery = Gallery(
      categoryId,
      galleryId,
      GalleryReadService.findMaxRankForCategory(categoryId) + 1,
      title,
      if (comment == "") None else Some(comment),
      "",
      List(),
      online,
      Versioning.newOne(authId))

    Logger.info("Gallery to be created : " + gallery)
    collection.insert(gallery)
  }

  // In that method we update everything that is not related to pictures
  def update(galleryId: Int,
             newCategoryId: Int,
             newTitle: String,
             newComment: Option[String],
             newOnline: Boolean,
             authId: String): Future[LastError] = {
    val galleryInDB = Await.result(GalleryReadService.findById(galleryId), 5 seconds).get
    val newGallery = galleryInDB.copy(
      categoryId = newCategoryId,
      title = newTitle,
      online = newOnline,
      versioning = galleryInDB.versioning.increment(authId),
      comment = newComment)

    collection.update(Json.obj("galleryId" -> galleryId), newGallery)
  }

  // In that method we update fields field by field
  def updateField(galleryId: Int, field: String, value: JsValue, authId: String): Future[LastError] = {
    val galleryInDB = Await.result(GalleryReadService.findById(galleryId), 5 seconds).get
    collection.update(
      Json.obj("galleryId" -> galleryId),
      Json.obj("$set" -> Json.obj(
        field -> value,
        "versioning.version" -> (galleryInDB.versioning.version + 1),
        "versioning.updateDate" -> new DateTime(), // It will be formatted as a String as converter is imported
        "versioning.updateUser" -> authId)))
  }

  // --------------------------------------------------------------
  // Methods related to gallery's pictures
  // --------------------------------------------------------------

  def addPictures(galleryId: Int, pictures: List[GalleryPic], authId: String): Future[LastError] = {
    val galleryInDB = Await.result(GalleryReadService.findById(galleryId), 5 seconds).get

    val array = JsArray(pictures.map(p => Json.toJson(p)).toList)

    // Here we mix "manual" update (use of $pushAll) and an update through a converter for Versioning class
    collection.update(
      Json.obj("galleryId" -> galleryId),
      Json.obj(
        "$pushAll" -> Json.obj("pictures" -> array),
        "$set" -> Json.obj("versioning" -> galleryInDB.versioning.increment(authId))))
  }

  def setPictures(galleryId: Int, pictures: List[GalleryPic], authId: String): Future[LastError] = {
    val array = JsArray(pictures.map(p => Json.toJson(p)).toList)
    updateField(galleryId, "pictures", array, authId)
  }

  /**
   * To update comment of a picture.
   * @param galleryId Gallery ID
   * @param index Index of picture in the list of pictures
   * @param comment New comment
   * @return
   */
  def updateComment(galleryId: Int, index: Int, comment: Option[String], authId: String): Future[LastError] = {

    // We retrieve gallery
    val future: Future[Option[JsObject]] =
      collection.
        find(Json.obj("galleryId" -> galleryId)).
        one[JsObject]
    val doc: JsObject = Await.result(future, 5 seconds).get

    val pictures: List[JsObject] = (doc \ "pictures").as[List[JsObject]]
    val picture: JsObject = pictures.apply(index)

    val newPicture = (picture - "comment") ++ (comment match {
      case None => Json.obj()
      case Some(c) => Json.obj("comment" -> c)
    })

    val listNew = pictures.take(index) ::: newPicture :: pictures.drop(index + 1)
    updateField(galleryId, "pictures", Json.toJson(listNew), authId)
  }

  // --------------------------------------------------------------
  // Misc
  // --------------------------------------------------------------

  def updateDatabase(): Unit = {
    //
  }
}
