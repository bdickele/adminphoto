package controllers.gallery

import play.api.mvc.{Action, Controller}
import models.gallery._
import scala.concurrent.{Await, Future}
import play.api.libs.concurrent.Execution.Implicits._
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import play.api.data.Forms._
import reactivemongo.bson.BSONString
import scala.Some
import play.api.data.Form

/**
 * Created by bdickele on 01/02/14.
 */

case class GalleryPicAction(galleryId: Int,
                            actionName: String,
                            picIndexes: List[Int])

object GalleryPicList extends Controller {

  val formMapping = mapping(
    "galleryId" -> number,
    "actionName" -> text,
    "picIndexes" -> list(number))(GalleryPicAction.apply)(GalleryPicAction.unapply)

  val form: Form[GalleryPicAction] = Form(formMapping)

  val MoveToTheBeginning = "MOVE_TO_THE_BEGINNING"
  val MoveToTheLeft = "MOVE_TO_THE_LEFT"
  val MoveToTheRight = "MOVE_TO_THE_RIGHT"
  val MoveToTheEnd = "MOVE_TO_THE_END"
  val Remove = "REMOVE"


  def view(galleryId: Int) = Action.async {
    val future = GalleryPicturesRW.findByGalleryId(galleryId)
    future.map {
      option => option match {
        case None => Galleries.couldNotFindGallery(galleryId)
        case Some(pics) => Ok(views.html.gallery.galleryPicList(pics, GalleryPicAction(galleryId, "", Nil)))
      }
    }
  }

  def viewAndSelect(galleryId: Int, indexes: String) = Action.async {
    val future = GalleryPicturesRW.findByGalleryId(galleryId)
    future.map {
      option => option match {
        case None => Galleries.couldNotFindGallery(galleryId)
        case Some(pics) => Ok(views.html.gallery.galleryPicList(pics,
          GalleryPicAction(galleryId, "", indexes.split("&").map(_.toInt).toList)))
      }
    }
  }

  def save() = Action {
    implicit request =>
      form.bindFromRequest.fold(

        formWithErrors => BadRequest("" + formWithErrors.errors.map(error => error.message).toList),

        form => {
          val galleryId = form.galleryId
          val actionName = form.actionName
          val selectedIndexes = form.picIndexes

          val newSelectedIndexes =
            if (selectedIndexes.isEmpty) {
              Nil
            } else if (actionName == MoveToTheBeginning || actionName == MoveToTheLeft ||
              actionName == MoveToTheRight || actionName == MoveToTheEnd ||
              actionName == Remove) {
              movePictures(galleryId, actionName, selectedIndexes)
            } else {
              Nil
            }

          newSelectedIndexes match {
            case Nil => Redirect(routes.GalleryPicList.view(galleryId))
            case List() => Redirect(routes.GalleryPicList.view(galleryId))
            case list => Redirect(routes.GalleryPicList.viewAndSelect(galleryId, newSelectedIndexes.mkString("&")))
          }
        })
  }

  /**
   * Move pictures of a gallery according to action required and list of indexes of selected pictures
   * @param galleryId Gallery ID
   * @param actionName Action required
   * @param selectedIndexes Indexes of selected pictures
   * @return Indexes of selected pictures in the new list
   */
  def movePictures(galleryId: Int, actionName: String, selectedIndexes: List[Int]): List[Int] = {
    val future = findGallery(galleryId)
    Await.result(future, Duration(5, TimeUnit.SECONDS)) match {
      case None => Nil
      case Some(gallery) => {
        val pics = gallery.pictures
        val selectedPics = selectedIndexes.map(i => pics.apply(i)).toList

        val nonSelectedIndexes = (0 until pics.size).toList.filterNot(i => selectedIndexes.contains(i))

        val indexesReordered = reorderIndexes(actionName, selectedIndexes, nonSelectedIndexes)
        val newPics = indexesReordered.map(i => pics.apply(i)).toList
        // Waiting for update otherwise screen could be displayed before being updated
        Await.result(GalleryPicturesRW.setPictures(galleryId, newPics), Duration(5, TimeUnit.SECONDS))

        // We return the list of indexes of selected pictures in the new list
        for {(pic, index) <- newPics.zipWithIndex
             if selectedPics.contains(pic)} yield index
      }
    }
  }

  /**
   *
   * @param actionName
   * @param selectedIndexes
   * @param nonSelectedIndexes
   * @return List of picture indexes as it should be after update. For instance, let us say we have 4 pictures
   *         in the gallery. User wants to shift 3rd picture (whose index is 2) to the left. That list would be
   *         List(0, 2, 1, 3) : index 2 is now in second position, and index 1 is now on 3rd position
   */
  def reorderIndexes(actionName: String, selectedIndexes: List[Int], nonSelectedIndexes: List[Int]): List[Int] = {

    def moveToTheLeftOrRight(acc: List[Int], selIndexes: List[Int], nonSelIndexes: List[Int],
                             condition: (Int, Int) => Boolean): List[Int] = selIndexes match {
      case Nil => acc ::: nonSelIndexes
      case selHead :: selTail => nonSelIndexes match {
        case Nil => acc ::: selIndexes
        case nonSelHead :: nonSelTail =>
          if (condition(selHead, acc.size)) moveToTheLeftOrRight(acc :+ nonSelHead, selIndexes, nonSelTail, condition)
          else moveToTheLeftOrRight(acc :+ selHead, selTail, nonSelIndexes, condition)
      }
    }

    actionName match {
      case Remove => nonSelectedIndexes

      case MoveToTheBeginning => selectedIndexes ::: nonSelectedIndexes

      case MoveToTheEnd => nonSelectedIndexes ::: selectedIndexes

      case MoveToTheLeft => moveToTheLeftOrRight(List(), selectedIndexes, nonSelectedIndexes,
        (selHead: Int, accSize: Int) => (selHead - 1) > accSize)

      case MoveToTheRight => {
        val finalSize = selectedIndexes.size + nonSelectedIndexes.size
        moveToTheLeftOrRight(List(), selectedIndexes.reverse, nonSelectedIndexes.reverse,
          (selHead: Int, accSize: Int) => (selHead + 2) < (finalSize - accSize)).reverse
      }
    }
  }

  /**
   * Change thumbnail of a gallery
   * @param galleryId Gallery ID
   * @param picIndex Index of picture that will be the new thumbnail
   * @return
   */
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
