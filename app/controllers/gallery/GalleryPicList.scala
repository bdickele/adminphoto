package controllers.gallery

import play.api.mvc.{SimpleResult, Action, Controller}
import models.gallery._
import scala.concurrent.Await
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import play.api.Logger
import reactivemongo.bson.BSONString

/**
 * Created by bdickele on 01/02/14.
 */
object GalleryPicList extends Controller {

  def view(galleryId: Int) = Action {
    val future = GalleryPicturesRW.findByGalleryId(galleryId)
    val galleryPics: GalleryPics = Await.result(future, Duration(5, TimeUnit.SECONDS)).get

    Ok(views.html.gallery.galleryPicList(galleryPics))
  }

  def up(galleryId: Int, picIndex: Int) = Action {
    findGallery(galleryId) match {
      case None => Galleries.couldNotFindGallery(galleryId)
      case Some(gallery) => {
        val pictures = gallery.pictures
        // First picture can't go higher, that's why we check that index is > 0
        if (picIndex > 0 && picIndex < pictures.length) {
          val (topHead, topTail) = pictures splitAt (picIndex - 1)
          val (bottomHead, bottomTail) = pictures splitAt (picIndex + 1)
          val newList = topHead ::: bottomHead.last :: topTail.head :: bottomTail
          GalleryPicturesRW.setPictures(galleryId, newList)
        }
        Redirect(routes.GalleryPicList.view(galleryId))
      }
    }
  }

  def down(galleryId: Int, picIndex: Int) = Action {
    findGallery(galleryId) match {
      case None => Galleries.couldNotFindGallery(galleryId)
      case Some(gallery) => {
        val pictures = gallery.pictures
        // Last picture can't go lower, that's why we check that index is < pictures.length - 1
        if (picIndex > -1 && picIndex < (pictures.length - 1)) {
          val (topHead, topTail) = pictures splitAt picIndex
          val (bottomHead, bottomTail) = pictures splitAt (picIndex + 2)
          val newList = topHead ::: bottomHead.last :: topTail.head :: bottomTail
          GalleryPicturesRW.setPictures(galleryId, newList)
        }
        Redirect(routes.GalleryPicList.view(galleryId))
      }
    }
  }

  def remove(galleryId: Int, picIndex: Int) = Action {
    GalleryPicturesRW.removePicture(galleryId, picIndex)
    Redirect(routes.GalleryPicList.view(galleryId))
  }

  def changeThumbnail(galleryId: Int, picIndex: Int) = Action {
    findGallery(galleryId) match {
      case None => Galleries.couldNotFindGallery(galleryId)
      case Some(gallery) => {
        val pictures = gallery.pictures
        if (picIndex > -1 && picIndex < pictures.length) {
          GalleryRW.updateField(galleryId, "thumbnail", BSONString(pictures.apply(picIndex).thumbnail))
        }
        Redirect(routes.GalleryPicList.view(galleryId))
      }
    }
  }

  def findGallery(galleryId: Int): Option[GalleryPics] =
    Await.result(GalleryPicturesRW.findByGalleryId(galleryId), Duration(5, TimeUnit.SECONDS))

  def incorrectIndex(galleryId: Int, index: Int): SimpleResult = {
    val message = "Incorrect index " + index + " for gallery ID " + galleryId
    Logger.error(message)
    BadRequest(message)
  }

}
