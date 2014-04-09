package service

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import scala.concurrent.{Await, Future}
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands.LastError
import scala.concurrent.duration._
import play.api.Logger
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.Some
import models.{GalleryPic, Gallery}
import service.mapper.GalleryMapper._

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
             online: Boolean): Future[LastError] = {
    val gallery = Gallery(
      categoryId,
      galleryId,
      GalleryReadService.findMaxRankForCategory(categoryId) + 1,
      title,
      if (comment == "") None else Some(comment),
      "",
      List(),
      online)

    Logger.info("Gallery to be created : " + gallery)
    collection.insert(Json.toJson(gallery))
  }

  /** In that method we update everything that is not related to pictures */
  def update(galleryId: Int,
             categoryId: Int,
             title: String,
             comment: Option[String],
             online: Boolean): Future[LastError] = {
    collection.update(Json.obj("galleryId" -> galleryId),
      Json.obj("$set" -> Json.obj(
        "categoryId" -> categoryId,
        "title" -> title,
        "online" -> online),
        comment match {
          case None => "$unset" -> Json.obj("comment" -> 1)
          case Some(d) => "$set" -> Json.obj("comment" -> d)
        }))
  }

  def updateField(galleryId: Int, field: String, value: JsValue): Future[LastError] =
    collection.update(
      Json.obj("galleryId" -> galleryId),
      Json.obj("$set" -> Json.obj(field -> value)))

  // --------------------------------------------------------------
  // Methods related to gallery's pictures
  // --------------------------------------------------------------

  def addPictures(galleryId: Int, pictures: List[GalleryPic]): Future[LastError] = {
    val array = JsArray(pictures.map(p => Json.toJson(p)).toList)
    collection.update(
      Json.obj("galleryId" -> galleryId),
      Json.obj("$pushAll" -> Json.obj("pictures" -> array)))
  }

  def setPictures(galleryId: Int, pictures: List[GalleryPic]): Future[LastError] = {
    val array = JsArray(pictures.map(p => Json.toJson(p)).toList)
    updateField(galleryId, "pictures", array)
  }

  /**
   * To update comment of a picture.
   * @param galleryId Gallery ID
   * @param index Index of picture in the list of pictures
   * @param comment New comment
   * @return
   */
  def updateComment(galleryId: Int, index: Int, comment: Option[String]): Future[LastError] = {

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
    updateField(galleryId, "pictures", Json.toJson(listNew))
  }
}
