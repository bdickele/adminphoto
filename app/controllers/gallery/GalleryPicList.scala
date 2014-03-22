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


  def view(galleryId: Int) = Action.async {
    val future = GalleryPicturesRW.findByGalleryId(galleryId)
    future.map {
      option => option match {
        case None => Galleries.couldNotFindGallery(galleryId)
        case Some(pics) => Ok(views.html.gallery.galleryPicList(pics, GalleryPicAction(galleryId, "", Nil)))
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
            if (actionName == "" || selectedIndexes.isEmpty) {
              Nil
            } else if (actionName == "MOVE_TO_THE_BEGINNING" || actionName == "MOVE_TO_THE_LEFT" ||
              actionName == "MOVE_TO_THE_RIGHT" || actionName == "MOVE_TO_THE_END" ||
              actionName == "REMOVE") {
              movePictures(galleryId, actionName, selectedIndexes)
            } else {
              Nil
            }

          val future = GalleryPicturesRW.findByGalleryId(galleryId)
          val galleryPics = Await.result(future, Duration(5, TimeUnit.SECONDS)).get
          Ok(views.html.gallery.galleryPicList(galleryPics, GalleryPicAction(galleryId, "", newSelectedIndexes)))
        })
  }

  /**
   *
   * @param actionName
   * @param selectedIndexes
   * @return Indexes of selected pictures in the new list
   */
  def movePictures(galleryId: Int, actionName: String, selectedIndexes: List[Int]): List[Int] = {
    val future = findGallery(galleryId)
    val option: Option[GalleryPics] = Await.result(future, Duration(5, TimeUnit.SECONDS))
    option match {
      case None => Nil
      case Some(gallery) => {
        val pics = gallery.pictures

        var selectedPicNames = List[String]() // Names of selected pictures
        for (i <- selectedIndexes) {
          selectedPicNames = selectedPicNames :+ pics.apply(i).web
        }

        val allIndexes = (0 until pics.size).toList
        val nonSelectedIndexes = allIndexes.filterNot(i => selectedIndexes.contains(i))

        val newIndexesAll = newListIndexes(actionName, allIndexes, selectedIndexes, nonSelectedIndexes)

        var newPics = List[GalleryPic]()
        for (i <- newIndexesAll) {
          newPics = newPics :+ pics.apply(i)
        }

        var newIndexesSelected = List[Int]() // Indexes of selected pictures in the new list
        // If I just removed pictures or I moved some to the beginning or to the end then nothing has to be selected
        if (actionName == "MOVE_TO_THE_LEFT" || actionName == "MOVE_TO_THE_RIGHT") {
          for {i <- 0 until newPics.size
               if selectedPicNames.contains(newPics.apply(i).web)} {
            newIndexesSelected = newIndexesSelected :+ i
          }
        }

        // Waiting for update otherwise screen could be displayed before being updated
        Await.result(GalleryPicturesRW.setPictures(galleryId, newPics), Duration(5, TimeUnit.SECONDS))
        newIndexesSelected
      }
    }
  }

  /**
   *
   * @param actionName
   * @param allIndexes
   * @param selectedIndexes
   * @param nonSelectedIndexes
   * @return List of picture indexes as it should be after update. For instance, let us say we have 4 pictures
   *         in the gallery. User wants to shift 3rd picture (whose index is 2) to the left. That list would be
   *         List(0, 2, 1, 3) : index 2 is now in second position, and index 1 is now on 3rd position
   */
  def newListIndexes(actionName: String, allIndexes: List[Int], selectedIndexes: List[Int],
                     nonSelectedIndexes: List[Int]): List[Int] = {
    var newIndexes = List[Int]()

    actionName match {
      case "REMOVE" => nonSelectedIndexes
      case "MOVE_TO_THE_LEFT" => {

        def dealWithLists(acc: List[Int], selIndexes: List[Int], nonSelIndexes: List[Int]): List[Int] = selIndexes match {
          case Nil => acc ::: nonSelIndexes
          case selHead :: selTail => {
            nonSelIndexes match {
              case Nil => acc ::: selIndexes // should not happen
              case nonSelHead :: nonSelTail => {
                if (nonSelHead < (selHead-1)) dealWithLists(acc :+ nonSelHead, selIndexes, nonSelTail)
                else dealWithLists(acc :+ selHead, selTail, nonSelIndexes)
              }
            }
          }
        }

        dealWithLists(List(), selectedIndexes, nonSelectedIndexes)
      }
      case "MOVE_TO_THE_RIGHT" => {

        def dealWithLists(acc: List[Int], selIndexes: List[Int], nonSelIndexes: List[Int]): List[Int] = selIndexes match {
          case Nil => acc ::: nonSelIndexes
          case selHead :: selTail => {
            nonSelIndexes match {
              case Nil => acc ::: selIndexes // should not happen
              case nonSelHead :: nonSelTail => {
                if (nonSelHead > (selHead+1)) dealWithLists(acc :+ nonSelHead, selIndexes, nonSelTail)
                else dealWithLists(acc :+ selHead, selTail, nonSelIndexes)
              }
            }
          }
        }

        dealWithLists(List(), selectedIndexes.reverse, nonSelectedIndexes.reverse).reverse
      }
      case "MOVE_TO_THE_BEGINNING" => selectedIndexes ::: nonSelectedIndexes
      case "MOVE_TO_THE_END" => nonSelectedIndexes ::: selectedIndexes
    }
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
