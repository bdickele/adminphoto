package controllers.gallery

import play.api.mvc.{Action, Controller}
import models.gallery._
import scala.concurrent.{Await, Future}
import reactivemongo.bson.BSONString
import play.api.libs.concurrent.Execution.Implicits._
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

/**
 * Created by bdickele on 01/02/14.
 */
object GalleryPicList extends Controller {

  var count = 0

  def view(galleryId: Int) = Action.async {
    val future = GalleryPicturesRW.findByGalleryId(galleryId)
    future.map {
      option => option match {
        case None => Galleries.couldNotFindGallery(galleryId)
        case Some(pics) => Ok(views.html.gallery.galleryPicList(pics))
      }
    }
  }

  def shiftPictures(galleryId: Int)
                   (isMovementAllowed: List[GalleryPic] => Boolean)
                   (transformation: List[GalleryPic] => List[GalleryPic]) = Action.async {
    val future = findGallery(galleryId)
    future.map {
      option =>
        option match {
          case None => Galleries.couldNotFindGallery(galleryId)
          case Some(gallery) => {
            if (isMovementAllowed(gallery.pictures)) {
              // Waiting for update before redirection
              Await.result(GalleryPicturesRW.setPictures(galleryId, transformation(gallery.pictures)), Duration(5, TimeUnit.SECONDS))
            }
            Redirect(routes.GalleryPicList.view(galleryId))
          }
        }
    }
  }

  def moveToTheBeginning(galleryId: Int, picIndex: Int) =
    shiftPictures(galleryId)(
      (pictures: List[GalleryPic]) => picIndex > 0 && picIndex < pictures.length)(
        (pictures: List[GalleryPic]) => {
          val (left, right) = pictures splitAt picIndex
          right.head :: left ::: right.tail
        })

  def moveToTheLeft(galleryId: Int, picIndex: Int) =
    shiftPictures(galleryId)(
      (pictures: List[GalleryPic]) => picIndex > 0 && picIndex < pictures.length)(
        (pictures: List[GalleryPic]) => {
          val (topHead, topTail) = pictures splitAt (picIndex - 1)
          val (bottomHead, bottomTail) = pictures splitAt (picIndex + 1)
          topHead ::: bottomHead.last :: topTail.head :: bottomTail
        })

  def moveToTheEnd(galleryId: Int, picIndex: Int) =
    shiftPictures(galleryId)(
      (pictures: List[GalleryPic]) => picIndex > -1 && picIndex < (pictures.length - 1))(
        (pictures: List[GalleryPic]) => {
          val (left, right) = pictures splitAt picIndex
          left ::: (right.tail :+ right.head)
        })

  def moveToTheRight(galleryId: Int, picIndex: Int) =
    shiftPictures(galleryId)(
      (pictures: List[GalleryPic]) => picIndex > -1 && picIndex < (pictures.length - 1))(
        (pictures: List[GalleryPic]) => {
          val (topHead, topTail) = pictures splitAt picIndex
          val (bottomHead, bottomTail) = pictures splitAt (picIndex + 2)
          topHead ::: bottomHead.last :: topTail.head :: bottomTail
        })

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
