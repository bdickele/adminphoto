package controllers.gallery

import play.api.mvc.{Action, Controller}
import play.api.data.Forms._
import play.api.data.Form
import models.gallery.{GalleryBasic, GalleryBasicRW, GalleryForm}
import controllers.category.Categories
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

/**
 * User: bdickele
 * Date: 1/26/14
 */
object GalleriesForm extends Controller {

  // ---------------------------------------------------------------
  // Mapping with all rules to check + Form[Mapping[CategoryForm]]
  // ---------------------------------------------------------------
  val galleryFormMapping = mapping(
    "categoryId" -> number.
      verifying("Unknown category",
        categoryId => Categories.findAllFromCacheOrDB().find(c => c.categoryId == categoryId) match {
          case None => false
          case Some => true
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
    //TODO tester que l'erreur s'affiche correctement dans le formulaire
    verifying("Another gallery with same title exists",
      gallery => findByTitle(gallery.title) match {
        case None => true
        case Some(g) => g.galleryId == gallery.galleryId
      })


  val galleryForm: Form[GalleryForm] = Form(galleryFormMapping)

  def create() = Action {
    // Let's retrieve ID of last category
    val categoryId = Categories.findAllFromCacheOrDB().maxBy(_.categoryId).categoryId

    Ok(views.html.gallery.galleryForm("Add a gallery",
      Categories.findAllFromCacheOrDB(),
      galleryForm.fill(GalleryForm.newOne(categoryId))))
  }

  def save() = Action {
    implicit request =>
      galleryForm.bindFromRequest.fold(

        // Validation error
        formWithErrors => Ok(views.html.gallery.galleryForm("Incorrect data for gallery", formWithErrors)),

        // Validation OK
        form => {
          val galleryId = form.galleryId

          //TODO Faire un GalleryRW avec un findById(categoryId: Int)

          /*
          val future: Future[List[GalleryBasic]] = GalleryBasicRW.findAll(form.categoryId)
          val galleries: List[GalleryBasic] = Await.result(future, Duration(10, TimeUnit.SECONDS))

          galleries.find(_.galleryId == galleryId) match {

            // Edition of an existing gallery
            case Some(gallery) => GalleryRW.update(
              category.copy(
                title = categoryForm.title,
                description = if (categoryForm.description.isEmpty) None else Some(categoryForm.description),
                online = categoryForm.online))

            // New category
            case None => CategoryRW.create(categoryForm.title, categoryForm.description, categoryForm.online)
          }
          */

          Redirect(routes.Galleries.view(form.categoryId))
        }
      )
  }

  // Required for form's validation

  def findByTitle(title: String): Option[GalleryBasic] =
    Await.result(GalleryBasicRW.findByTitle(title), Duration(5, TimeUnit.SECONDS))
}
