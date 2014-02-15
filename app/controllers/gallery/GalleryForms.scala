package controllers.gallery

import play.api.mvc.{SimpleResult, Action, Controller}
import play.api.data.Forms._
import play.api.data.Form
import controllers.category.Categories
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import scala.Some
import models.gallery.{Gallery, GalleryRW, GalleryForm}
import scala.util.{Success, Failure}
import reactivemongo.bson.BSONInteger
import models.category.Category

/**
 * User: bdickele
 * Date: 1/26/14
 */
object GalleryForms extends Controller {

  // ---------------------------------------------------------------
  // Mapping with all rules to check + Form[Mapping[GalleryForm]]
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
    "comment" -> text.
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
    Ok(views.html.gallery.galleryForm("New gallery",
      Categories.findAllFromCacheOrDB(),
      galleryForm.fill(GalleryForm.newOne(categoryId))))
  }

  def edit(galleryId: Int) = Action {
    Await.result(GalleryRW.findById(galleryId), Duration(5, TimeUnit.SECONDS)) match {
      case Some(gallery) => Ok(views.html.gallery.galleryForm("Gallery \"" + gallery.title + "\"",
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
                if (form.comment.isEmpty) None else Some(form.comment),
                form.online)
              Redirect(routes.GalleryPicList.view(form.galleryId))
            }

            // New gallery
            case None => {
              val galleryId = GalleryRW.findMaxGalleryId + 1
              GalleryRW.create(form.categoryId, galleryId, form.title, form.year, form.month,
                form.comment, form.online)
              Redirect(routes.GalleryPicSelection.view(galleryId, "", ""))
            }
          }
        }
      )
  }

  // Required by form's validation
  def findByTitle(title: String): Option[Gallery] =
    Await.result(GalleryRW.findByTitle(title), Duration(5, TimeUnit.SECONDS))

  /**
   * Redirect to previous gallery of passed gallery ID
   * @param galleryId
   * @return
   */
  def previousGallery(galleryId: Int) = Action {
    val future = GalleryRW.findById(galleryId)
    val gallery = Await.result(future, Duration(5, TimeUnit.SECONDS)).get

    val previousGalleryId: Int = gallery.rank match {
      // First gallery of the category : let's pick up the last gallery of the previous category
      case 0 => lastGalleryIdOfPreviousCategory(gallery.categoryId)

      case _ => {
        val galleries = Galleries.findAll(gallery.categoryId)

        // Let's retrieve our category
        galleries.find(_.galleryId == galleryId) match {
          case Some(gallery) => {
            val galleryRank = gallery.rank

            // List is sorted by rank, thus we pick up the first gallery whose rank is < gallery's rank
            galleries.find(_.rank < galleryRank) match {
              case None => lastGalleryIdOfPreviousCategory(gallery.categoryId)
              case Some(otherGallery) => otherGallery.galleryId
            }
          }
          case None => -1
        }
      }
    }

    Redirect(routes.GalleryPicList.view(previousGalleryId))
  }

  /**
   * Retrieves ID of last gallery of category before category of passed categoryId.
   * If categoryId stands for first category, then last category (the most recent) is selected
   * @param categoryId
   */
  def lastGalleryIdOfPreviousCategory(categoryId: Int): Int = {
    // Categories are sorted by rank, what means the most recent one is the first
    val categories: List[Category] = Categories.findAllFromCacheOrDB()

    val category = categories.find(_.categoryId == categoryId).get
    val rank = category.rank

    val newCategoryId: Int = rank match {
      case 0 => categories.head.categoryId
      case n => categories.find(_.rank < category.rank).get.categoryId
    }

    val future = GalleryRW.findAll(newCategoryId)
    val galleries = Await.result(future, Duration(5, TimeUnit.SECONDS))

    // In case category doesn't contain any gallery
    galleries match {
      case Nil => lastGalleryIdOfPreviousCategory(newCategoryId)
      case _ => galleries.head.galleryId
    }
  }

  /**
   * Redirect to next gallery of passed gallery ID
   * @param galleryId
   * @return
   */
  def nextGallery(galleryId: Int) =  Action {
    val future = GalleryRW.findById(galleryId)
    val gallery = Await.result(future, Duration(5, TimeUnit.SECONDS)).get

    // We need to sort the galleries from the oldest to the most recent => we have to reverse it
    val galleries = Galleries.findAll(gallery.categoryId).reverse
    val lastIndex = galleries.length - 1
    val galleryIndex = galleries.indexWhere(_.galleryId == galleryId)

    val nextGalleryId: Int = galleryIndex match {
      // Last gallery of the category : let's pick up the first gallery of the next category
      case `lastIndex` => firstGalleryIdOfNextCategory(gallery.categoryId)
      case _ => galleries.apply(galleryIndex + 1).galleryId
    }

    Redirect(routes.GalleryPicList.view(nextGalleryId))
  }

  /**
   * Retrieves ID of first gallery of category after category of passed categoryId.
   * If categoryId stands for last category, then first category (the oldest) is selected
   * @param categoryId
   */
  def firstGalleryIdOfNextCategory(categoryId: Int): Int = {
    // Categories are sorted by rank, what means the most recent one is the first: let's reverse it
    val categories: List[Category] = Categories.findAllFromCacheOrDB().reverse

    val categoryIndex = categories.indexWhere(_.categoryId == categoryId)
    val lastIndex = categories.length - 1

    val newCategoryId: Int = categoryIndex match {
      case `lastIndex` => categories.head.categoryId
      case n => categories.apply(categoryIndex + 1).categoryId
    }

    val future = GalleryRW.findAll(newCategoryId)
    val galleries = Await.result(future, Duration(5, TimeUnit.SECONDS))

    // In case category doesn't contain any gallery
    galleries match {
      case Nil => firstGalleryIdOfNextCategory(newCategoryId)
      case _ => galleries.last.galleryId
    }
  }
}
