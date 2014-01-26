package controllers.gallery

import play.api.mvc.{Action, Controller}
import play.api.data.Forms._
import play.api.data.Form
import models.gallery.GalleryForm
import controllers.category.Categories

/**
 * User: bdickele
 * Date: 1/26/14
 */
object GalleriesForm extends Controller {

  // ---------------------------------------------------------------
  // Mapping with all rules to check + Form[Mapping[CategoryForm]]
  // ---------------------------------------------------------------
  val galleryFormMapping = mapping(
    "categoryId" -> number,
    "galleryId" -> number,
    "title" -> nonEmptyText.
      verifying("Title cannot exceed 50 characters", _.length <= 50),
    "year"-> number.
      verifying("Incorrect value for year (has to be > 1970)", _ > 1970),
    "month" -> number.
      verifying("Incorrect value for month", {m => m > 0 && m < 13}),
    "description" -> text.
      verifying("Description cannot exceed 500 characters", _.length <= 500),
    "online" -> boolean)(GalleryForm.apply)(GalleryForm.unapply)

    /*.

    // Title is to be unique
    //TODO tester que l'erreur s'affiche correctement dans le formulaire
    verifying("Another category with same title exists",
      gallery => findByTitle(gallery.title) match {
        case None => true
        case Some(g) => g.galleryId == gallery.galleryId
      })
      */

  val galleryForm: Form[GalleryForm] = Form(galleryFormMapping)

  def create() = Action {
    // Let's retrieve ID of last category
    val categoryId = Categories.findAllFromCacheOrDB().maxBy(_.categoryId).categoryId

    Ok(views.html.gallery.galleryForm("Add a gallery",
      Categories.findAllFromCacheOrDB(),
      galleryForm.fill(GalleryForm.newOne(categoryId))))
  }

  def save() = TODO
}
