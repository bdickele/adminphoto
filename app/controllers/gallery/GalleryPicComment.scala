package controllers.gallery

import play.api.mvc.{Action, Controller}
import play.api.data.Forms._
import play.api.data.Form
import models.gallery.{GalleryRW, Gallery, GalleryPicturesRW}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Await}
import util.Const
import controllers.category.Categories

/**
 * Created by bdickele
 * Date: 2/2/14
 */
case class GalleryPicComment(galleryId: Int,
                             index: Int,
                             webComplete: String,
                             comment: Option[String])

object GalleryPicComment extends Controller {

  val formMapping = mapping(
    "galleryId" -> number,
    "index" -> number,
    "webComplete" -> ignored(""),
    "comment" -> optional(text).
      verifying("Comment cannot exceed 200 characters", _ match {
      case None => true
      case Some(d) => d.length <= 200
    }))(GalleryPicComment.apply)(GalleryPicComment.unapply)

  val picForm: Form[GalleryPicComment] = Form(formMapping)


  def view(galleryId: Int, index: Int) = Action {
    val future = GalleryPicturesRW.findByGalleryId(galleryId)
    val galleryPics = Await.result(future, Duration(5, TimeUnit.SECONDS)).get
    val galleryPic = galleryPics.pictures.apply(index)

    Ok(views.html.gallery.galleryPicComment(
      picForm.fill(GalleryPicComment(
        galleryId,
        index,
        Const.WebRoot + galleryPic.web,
        galleryPic.comment))))
  }

  def save() = Action {
    implicit request =>
      picForm.bindFromRequest.fold(

        // Validation error
        formWithErrors => BadRequest("Unexpected technical error "),

        // Validation OK
        form => {
          GalleryPicturesRW.updateComment(form.galleryId, form.index, form.comment)

          //TODO Passer à l'image suivante
          Redirect(routes.GalleryPicList.view(form.galleryId))
        }
      )
  }

}
