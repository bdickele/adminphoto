package controllers.gallery

import play.api.mvc.Controller
import play.api.data.Forms._
import play.api.data.Form
import util.Const
import securesocial.core.SecureSocial
import service.{GalleryReadService, GalleryWriteService}
import play.api.libs.concurrent.Execution.Implicits._
import models.GalleryPicComment


/**
 * Controller dedicated to page where we update picture's comment
 * Date: 2/2/14
 */
object GalleryPicComments extends Controller with SecureSocial {

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
      GalleryReadService.findById(galleryId).map {
      _ match {
        case Some(gallery) =>
          val realIndex = if (index < 0 || index > (gallery.pictures.length - 1)) 0 else index

          val galleryPic = gallery.pictures.apply(realIndex)

          Ok(views.html.gallery.galleryPicComment(
            picForm.fill(GalleryPicComment(
              gallery.categoryId,
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
          GalleryWriteService.updateComment(form.galleryId, form.index, form.comment)
          // Once comment is saved we moved to next picture
          Redirect(routes.GalleryPicComments.view(form.galleryId, form.index + 1))
        }
      )
  }

}
