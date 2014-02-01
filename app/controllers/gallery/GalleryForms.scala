package controllers.gallery

import play.api.mvc.{Action, Controller}
import play.api.data.Forms._
import play.api.data.Form
import controllers.category.Categories
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import scala.Some
import models.gallery.{Gallery, GalleryRW, GalleryForm}
import scala.util.{Success, Failure}

/**
 * User: bdickele
 * Date: 1/26/14
 */
object GalleryForms extends Controller {

  // ---------------------------------------------------------------
  // Mapping with all rules to check + Form[Mapping[CategoryForm]]
  // ---------------------------------------------------------------
  val formMapping = mapping(
    "categoryId" -> number.
      verifying("Unknown category",
        categoryId => Categories.findAllFromCacheOrDB().find(c => c.categoryId == categoryId) match {
          case None => false
          case Some(_) => true
        }),
    "galleryId" -> number,
    "title" -> nonEmptyText.
      verifying("Title cannot exceed 50 characters", _.length <= 50),
    "year" -> number.
      verifying("Incorrect value for year (has to be > 1970)", _ > 1970),
    "month" -> number.
      verifying("Incorrect value for month", m => m > 0 && m < 13),
    "description" -> text.
      verifying("Description cannot exceed 500 characters", _.length <= 500),
    "online" -> boolean)(GalleryForm.apply)(GalleryForm.unapply).

    // Title is to be unique
    verifying("Another gallery with same title exists",
      gallery => findByTitle(gallery.title) match {
        case None => true
        case Some(g) => g.galleryId == gallery.galleryId
      })

  val galleryForm: Form[GalleryForm] = Form(formMapping)


  def create(categoryId: Int) = Action {
    Ok(views.html.gallery.galleryForm("Add a gallery",
      Categories.findAllFromCacheOrDB(),
      galleryForm.fill(GalleryForm.newOne(categoryId))))
  }

  def edit(galleryId: Int) = Action {
    Await.result(GalleryRW.findById(galleryId), Duration(5, TimeUnit.SECONDS)) match {
      case Some(gallery) => Ok(views.html.gallery.galleryForm("Gallery edition",
        Categories.findAllFromCacheOrDB(),
        galleryForm.fill(GalleryForm(gallery))))
      case None => Galleries.couldNotFindGallery(galleryId)
    }
  }

  def save() = Action {
    implicit request =>
      galleryForm.bindFromRequest.fold(

        // Validation error
        formWithErrors => Ok(views.html.gallery.galleryForm("Incorrect data for gallery",
          Categories.findAllFromCacheOrDB(), formWithErrors)),

        // Validation OK
        form => {
          val galleryId = form.galleryId
          val future: Future[Option[Gallery]] = GalleryRW.findById(galleryId)
          val option: Option[Gallery] = Await.result(future, Duration(10, TimeUnit.SECONDS))

          option match {

            // Edition of an existing gallery
            case Some(gallery) => {
              GalleryRW.update(
                form.galleryId,
                form.categoryId,
                form.title,
                form.year,
                form.month,
                if (form.description.isEmpty) None else Some(form.description),
                form.online)
              Redirect(routes.Galleries.view(form.categoryId))
            }

            // New gallery
            case None => {
              val galleryId = GalleryRW.findMaxGalleryId + 1
              GalleryRW.create(form.categoryId, galleryId, form.title, form.year, form.month,
                form.description, form.online)
              Redirect(routes.GalleryPicsForms.edit(galleryId))
            }
          }
        }
      )
  }

  // Required by form's validation
  def findByTitle(title: String): Option[Gallery] =
    Await.result(GalleryRW.findByTitle(title), Duration(5, TimeUnit.SECONDS))
}
