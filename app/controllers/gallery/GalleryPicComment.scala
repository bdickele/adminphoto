package controllers.gallery

import play.api.mvc.{Action, Controller}
import play.api.data.Forms._
import play.api.data.Form
import models.gallery.GalleryPicturesRW
import util.Const
import play.api.libs.concurrent.Execution.Implicits._
import securesocial.core.SecureSocial

/**
 * Created by bdickele
 * Date: 2/2/14
 */
case class GalleryPicComment(categoryId: Int,
                             galleryId: Int,
                             index: Int,
                             webComplete: String,
                             comment: Option[String])

object GalleryPicComment extends Controller with SecureSocial {

  val formMapping = mapping(
    "categoryId" -> number,
    "galleryId" -> number,
    "index" -> number,
    "webComplete" -> ignored(""),
    "comment" -> optional(text).
      verifying("Comment cannot exceed 200 characters", _ match {
      case None => true
      case Some(d) => d.length <= 200
    }))(GalleryPicComment.apply)(GalleryPicComment.unapply)

  val picForm: Form[GalleryPicComment] = Form(formMapping)


  def view(galleryId: Int, index: Int) = SecuredAction.async {
    implicit request =>
    GalleryPicturesRW.findByGalleryId(galleryId).map {
      _ match {
        case Some(galleryPics) =>
          val realIndex = if (index < 0 || index > (galleryPics.pictures.length - 1)) 0 else index

          val galleryPic = galleryPics.pictures.apply(realIndex)

          Ok(views.html.gallery.galleryPicComment(
            picForm.fill(GalleryPicComment(
              galleryPics.categoryId,
              galleryId,
              realIndex,
              Const.WebRoot + galleryPic.web,
              galleryPic.comment))))

        case _ => Galleries.couldNotFindGallery(galleryId)
      }
    }
  }

  def save() = SecuredAction {
    implicit request =>
      picForm.bindFromRequest.fold(

        // Validation error
        formWithErrors => BadRequest(views.html.badRequest("" + formWithErrors.errors.map(error => error.message).toList)),

        // Validation OK
        form => {
          GalleryPicturesRW.updateComment(form.galleryId, form.index, form.comment)
          // Once comment is saved we moved to next picture
          Redirect(routes.GalleryPicComment.view(form.galleryId, form.index + 1))
        }
      )
  }

}
