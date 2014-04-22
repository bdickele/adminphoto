package controllers.gallery

import play.api.mvc.Controller
import play.api.data.Forms._
import play.api.data.Form
import controllers.category.Categories
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import language.postfixOps
import play.api.libs.concurrent.Execution.Implicits._
import securesocial.core.{SecuredRequest, SecureSocial}
import service.{GalleryReadService, GalleryWriteService}
import models._
import models.WithRole
import scala.Some
import models.Category

/**
 * Controller for actions related to gallery's form (creation and edition)
 * bdickele
 */
object GalleryForms extends Controller with SecureSocial {

  // ---------------------------------------------------------------
  // Mapping with all rules to check + Form[Mapping[GalleryForm]]
  // ---------------------------------------------------------------
  val formMapping = mapping(
    "categoryId" -> number.
      verifying("Unknown category",
        categoryId =>
          Categories.findAllFromCacheOrDB().find(c => c.categoryId == categoryId) match {
            case None => false
            case Some(_) => true
          }),
    "galleryId" -> number,
    "title" -> nonEmptyText.
      verifying("Title cannot exceed 70 characters", _.length <= 70),
    "comment" -> text.
      verifying("Description cannot exceed 3000 characters", _.length <= 3000),
    "online" -> boolean)(GalleryForm.apply)(GalleryForm.unapply).

    // Title is to be unique
    verifying("Another gallery with same title exists",
      gallery =>
        findByTitle(gallery.title) match {
          case None => true
          case Some(g) => g.galleryId == gallery.galleryId
        })

  val galleryForm: Form[GalleryForm] = Form(formMapping)


  def create(categoryId: Int) = SecuredAction(WithRole(Role.Writer)) { implicit request =>
    val categories: List[Category] = Categories.findAllFromCacheOrDB()

    categories.find(_.categoryId == categoryId) match {

      case Some(category) =>
        Ok(views.html.gallery.galleryForm("New gallery",
          Categories.findAllFromCacheOrDB(),
          galleryForm.fill(GalleryForm.newOne(categoryId))))

      case None => Categories.couldNotFindCategory(categoryId)
    }
  }

  def edit(galleryId: Int) = SecuredAction.async {
    implicit request =>
      val future = GalleryReadService.findById(galleryId)

      future.map {
        case Some(gallery) => Ok(views.html.gallery.galleryForm(
          gallery.title,
          Categories.findAllFromCacheOrDB(),
          galleryForm.fill(GalleryForm(gallery))))
        case None => Galleries.couldNotFindGallery(galleryId)
      }
  }

  def save() = SecuredAction(WithRole(Role.Writer)) { implicit request =>
    galleryForm.bindFromRequest.fold(

      // Validation error
      formWithErrors =>
        Ok(views.html.gallery.galleryForm("Incorrect data for gallery",
          Categories.findAllFromCacheOrDB(), formWithErrors)),

      // Validation OK
      form => {
        val galleryId = form.galleryId
        val future: Future[Option[Gallery]] = GalleryReadService.findById(galleryId)
        val option: Option[Gallery] = Await.result(future, 5 seconds)

        option match {

          // Edition of an existing gallery
          case Some(gallery) =>
            GalleryWriteService.update(
              form.galleryId,
              form.categoryId,
              form.title,
              if (form.comment.isEmpty) None else Some(form.comment),
              form.online,
              BackEndUser.user(request).authId)
            Redirect(routes.GalleryPicList.pictures(form.galleryId))

          // New gallery
          case None =>
            val newGalleryId = GalleryReadService.findMaxGalleryId + 1
            GalleryWriteService.create(form.categoryId, newGalleryId, form.title, form.comment, form.online,
              BackEndUser.user(request).authId)
            Redirect(routes.GalleryPicSelection.pictures(newGalleryId, "", ""))
        }
      }
    )
  }

  // Required by form's validation
  def findByTitle(title: String): Option[Gallery] =
    Await.result(GalleryReadService.findByTitle(title), 5 seconds)

  /**
   * Redirect to previous gallery of passed gallery ID
   * @param galleryId Gallery ID
   * @return
   */
  def previousGallery(galleryId: Int) = SecuredAction.async {
    implicit request =>
      val gallery = Await.result(GalleryReadService.findById(galleryId), 5 seconds).get

      val previousFuture = GalleryReadService.findPreviousGalleryInCategory(gallery.categoryId, gallery.rank)

      val previousGallery: Future[Gallery] = previousFuture.map {
        case Some(g) => g
        case None => lastGalleryOfPreviousCategory(gallery.categoryId)
      }

      previousGallery.map(g => Redirect(routes.GalleryPicList.pictures(g.galleryId)))
  }

  /**
   * Retrieves ID of last gallery of category before category of passed categoryId.
   * If categoryId stands for first category, then last category (the most recent) is selected
   * @param categoryId Category ID
   */
  def lastGalleryOfPreviousCategory(categoryId: Int): Gallery = {
    // Categories are sorted by rank, what means the most recent one is the first
    val categories: List[Category] = Categories.findAllFromCacheOrDB()

    val category = categories.find(_.categoryId == categoryId).get
    val rank = category.rank

    val newCategoryId: Int = rank match {
      case 0 => categories.head.categoryId
      case n => categories.find(_.rank < category.rank).get.categoryId
    }

    val futureGallery = GalleryReadService.findLastGalleryOfCategory(newCategoryId)

    // In case category doesn't contain any gallery
    Await.result(futureGallery, 5 seconds) match {
      case None => lastGalleryOfPreviousCategory(newCategoryId)
      case Some(g) => g
    }
  }

  /**
   * Redirect to next gallery of passed gallery ID
   * @param galleryId Gallery ID
   * @return
   */
  def nextGallery(galleryId: Int) = SecuredAction.async {
    implicit request =>
      val gallery = Await.result(GalleryReadService.findById(galleryId), 5 seconds).get

      val nextFuture = GalleryReadService.findNextGalleryInCategory(gallery.categoryId, gallery.rank)
      val nextGallery: Future[Gallery] = nextFuture.map {
        case Some(g) => g
        case None => firstGalleryOfNextCategory(gallery.categoryId)
      }

      nextGallery.map(g => Redirect(routes.GalleryPicList.pictures(g.galleryId)))
  }

  /**
   * Retrieves ID of first gallery of category after category of passed categoryId.
   * If categoryId stands for last category, then first category (the oldest) is selected
   * @param categoryId Category ID
   */
  def firstGalleryOfNextCategory(categoryId: Int): Gallery = {
    // Categories are sorted by rank, what means the most recent one is the first: let's reverse it
    val categories: List[Category] = Categories.findAllFromCacheOrDB().reverse

    val categoryIndex = categories.indexWhere(_.categoryId == categoryId)
    val lastIndex = categories.length - 1

    val newCategoryId: Int = categoryIndex match {
      case `lastIndex` => categories.head.categoryId
      case n => categories.apply(categoryIndex + 1).categoryId
    }

    val futureGallery = GalleryReadService.findFirstGalleryOfCategory(newCategoryId)

    // In case category doesn't contain any gallery
    Await.result(futureGallery, 5 seconds) match {
      case None => firstGalleryOfNextCategory(newCategoryId)
      case Some(g) => g
    }
  }
}