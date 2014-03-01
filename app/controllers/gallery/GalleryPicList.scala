package controllers.gallery

import play.api.mvc.{Action, Controller}
import models.gallery._
import scala.concurrent.Future
import reactivemongo.bson.BSONString
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Created by bdickele on 01/02/14.
 */
object GalleryPicList extends Controller {

  def view(galleryId: Int) = Action.async {
    val future = GalleryPicturesRW.findByGalleryId(galleryId)
    future.map {
      option => option match {
        case None => Galleries.couldNotFindGallery(galleryId)
        case Some(pics) => Ok(views.html.gallery.galleryPicList(pics))
      }
    }
  }

  def up(galleryId: Int, picIndex: Int) = Action.async {
    val future = findGallery(galleryId)
    future.map {
      option =>
        option match {
          case None => Galleries.couldNotFindGallery(galleryId)
          case Some(gallery) => {
            val pictures = gallery.pictures

            // First picture can't go higher, that's why we check that index is > 0
            val movementPossible = picIndex > 0 && picIndex < pictures.length
            if (movementPossible) {
              val (topHead, topTail) = pictures splitAt (picIndex - 1)
              val (bottomHead, bottomTail) = pictures splitAt (picIndex + 1)
              val newList = topHead ::: bottomHead.last :: topTail.head :: bottomTail
              GalleryPicturesRW.setPictures(galleryId, newList)
            }

            val anchor = if (movementPossible) {
              picIndex - 1
            } else picIndex
            Redirect(routes.GalleryPicList.view(galleryId))
          }
        }
    }
  }

  def down(galleryId: Int, picIndex: Int) = Action.async {
    val future = findGallery(galleryId)
    future.map {
      option => option match {
        case None => Galleries.couldNotFindGallery(galleryId)
        case Some(gallery) => {
          val pictures = gallery.pictures

          // Last picture can't go lower, that's why we check that index is < pictures.length - 1
          val movementPossible = picIndex > -1 && picIndex < (pictures.length - 1)
          if (movementPossible) {
            val (topHead, topTail) = pictures splitAt picIndex
            val (bottomHead, bottomTail) = pictures splitAt (picIndex + 2)
            val newList = topHead ::: bottomHead.last :: topTail.head :: bottomTail
            GalleryPicturesRW.setPictures(galleryId, newList)
          }

          val anchor = if (movementPossible) {picIndex + 1} else picIndex
          Redirect(routes.GalleryPicList.view(galleryId))
        }
      }
    }
  }

  def remove(galleryId: Int, picIndex: Int) = Action {
    GalleryPicturesRW.removePicture(galleryId, picIndex)
    Redirect(routes.GalleryPicList.view(galleryId))
  }

  def changeThumbnail(galleryId: Int, picIndex: Int) = Action.async {
    val future = findGallery(galleryId)
    future.map {
      option => option match {
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
  }

  def findGallery(galleryId: Int): Future[Option[GalleryPics]] =
    GalleryPicturesRW.findByGalleryId(galleryId)

}
