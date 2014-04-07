package controllers.gallery

import play.api.mvc.Controller
import scala.concurrent.Await
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.data.Forms._
import scala.Some
import play.api.data.Form
import securesocial.core.SecureSocial
import service.{GalleryReadService, GalleryWriteService}
import play.api.libs.json.Json
import models.{Role, WithRole}

/**
 * Some actions related to pictures : move to the left/right/end/beginning + change thumbnail
 * bdickele
 */
case class GalleryPicAction(galleryId: Int,
                            actionName: String,
                            picIndexes: List[Int])

object GalleryPicList extends Controller with SecureSocial {

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


  def view(galleryId: Int) = SecuredAction.async {
    implicit request =>
      GalleryReadService.findById(galleryId).map {
        _ match {
          case None => Galleries.couldNotFindGallery(galleryId)
          case Some(pics) => Ok(views.html.gallery.galleryPicList(pics, GalleryPicAction(galleryId, "", Nil)))
        }
      }
  }

  def viewAndSelect(galleryId: Int, indexes: String) = SecuredAction(WithRole(Role.Writer)).async {
    implicit request =>
      GalleryReadService.findById(galleryId).map {
        _ match {
          case None => Galleries.couldNotFindGallery(galleryId)
          case Some(pics) => Ok(views.html.gallery.galleryPicList(pics,
            GalleryPicAction(galleryId, "", indexes.split("&").map(_.toInt).toList)))
        }
      }
  }

  def save() = SecuredAction(WithRole(Role.Writer)) {
    implicit request =>
      form.bindFromRequest.fold(

        formWithErrors => BadRequest(views.html.badRequest("" + formWithErrors.errors.map(error => error.message).toList)),

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
    val future = GalleryReadService.findById(galleryId)
    Await.result(future, 5 seconds) match {
      case None => Nil
      case Some(gallery) =>
        val pics = gallery.pictures
        val selectedPics = selectedIndexes.map(i => pics.apply(i)).toList

        val nonSelectedIndexes = (0 until pics.size).toList.filterNot(i => selectedIndexes.contains(i))

        val indexesReordered = reorderIndexes(actionName, selectedIndexes, nonSelectedIndexes)
        val newPics = indexesReordered.map(i => pics.apply(i)).toList
        // Waiting for update otherwise screen could be displayed before being updated
        Await.result(GalleryWriteService.setPictures(galleryId, newPics), 5 seconds)

        // We return the list of indexes of selected pictures in the new list
        for {(pic, index) <- newPics.zipWithIndex
             if selectedPics.contains(pic)} yield index
    }
  }

  /**
   *
   * @param actionName Action name
   * @param selectedIndexes Indexes of selected pictures
   * @param nonSelectedIndexes Indexes of non selected pictures
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

      case MoveToTheRight =>
        val finalSize = selectedIndexes.size + nonSelectedIndexes.size
        moveToTheLeftOrRight(List(), selectedIndexes.reverse, nonSelectedIndexes.reverse,
          (selHead: Int, accSize: Int) => (selHead + 2) < (finalSize - accSize)).reverse
    }
  }

  /**
   * Change thumbnail of a gallery
   * @param galleryId Gallery ID
   * @param picIndex Index of picture that will be the new thumbnail
   * @return
   */
  def changeThumbnail(galleryId: Int, picIndex: Int) = SecuredAction(WithRole(Role.Writer)).async {
    implicit request =>
      GalleryReadService.findById(galleryId).map {
        _ match {
          case None => Galleries.couldNotFindGallery(galleryId)
          case Some(gallery) =>
            val pictures = gallery.pictures
            if (picIndex > -1 && picIndex < pictures.length) {
              GalleryWriteService.updateField(galleryId, "thumbnail", Json.toJson(pictures.apply(picIndex).thumbnail))
            }
            Redirect(routes.GalleryPicList.view(galleryId))
        }
      }
  }

}
