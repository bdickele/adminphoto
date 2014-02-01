package controllers.gallery

import play.api.mvc.Controller
import play.api.data.Forms._
import play.api.data.Form
import models.gallery.{GalleryPicForm, GalleryPicsForm}

/**
 * Created by bdickele on 01/02/14.
 */
object GalleryPicsForms extends Controller {

  val formMapping = mapping(
    "galleryId" -> number,
    "galleryTitle" -> text,
    "thumbnail" -> nonEmptyText,
    "pictures" -> list(mapping(
      "thumbnail" -> text,
      "web" -> text,
      "print" -> text,
      "description" -> text)(GalleryPicForm.apply)(GalleryPicForm.unapply)
    ))(GalleryPicsForm.apply)(GalleryPicsForm.unapply)

  val picturesForm: Form[GalleryPicsForm] = Form(formMapping)

  def edit(galleryId: Int) = TODO

}
