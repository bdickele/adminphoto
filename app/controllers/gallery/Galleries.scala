package controllers.gallery

import play.api.mvc.{SimpleResult, Controller}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import language.postfixOps
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import controllers.category.Categories
import securesocial.core.SecureSocial
import service.{GalleryReadService, GalleryWriteService}
import play.api.libs.json.Json
import models._
import models.Gallery
import models.WithRole
import scala.Some
import models.Category

/**
 * Controller for galleries
 * bdickele
 */
object Galleries extends Controller with SecureSocial {

  def galleries(passedCategoryId: Int = -1) = SecuredAction.async { implicit request =>
    val categories: List[Category] = Categories.findAllFromCacheOrDB()
    val categoryId = if (passedCategoryId > 0) passedCategoryId else categories.head.categoryId

    // In case URL contains an incorrect category ID
    categories.exists(c => c.categoryId == categoryId) match {
      case false => Future.successful(Categories.couldNotFindCategory(categoryId))

      case true => GalleryReadService.findAll(categoryId).map {
        galleries => Ok(views.html.gallery.gallery(categoryId, categories, galleries))
      }.recover {
        case e =>
          Logger.error(e.getMessage)
          BadRequest(views.html.global.badRequest(e.getMessage))
      }
    }
  }

  def refresh(categoryId: Int) = SecuredAction { implicit request =>
    Redirect(routes.Galleries.galleries(categoryId))
  }

  /** A gallery has to "go up" in the hierarchy of galleries */
  def up(galleryId: Int) = SecuredAction(WithRole(Role.Writer)).async { implicit request =>
    val categoryId = GalleryReadService.findCategoryId(galleryId)
    findAllFuture(categoryId).map { galleries =>
      galleries.find(_.galleryId == galleryId) match {
        case Some(gallery) =>
          val galleryRank = gallery.rank
          val authId = BackEndUser.user(request).authId

          // List is sorted by rank: we reverse it and pick up the first category whose rank is > category's rank
          // find method returns an Option, but if it returns None I don't do nothing
          // That's why I directly use map method
          galleries.reverse.find(_.rank > galleryRank).map { galleryAbove =>
            GalleryWriteService.updateField(galleryId, "rank", Json.toJson(galleryRank + 1), authId)
            GalleryWriteService.updateField(galleryAbove.galleryId, "rank", Json.toJson(galleryRank), authId)
          }

          Redirect(routes.Galleries.galleries(categoryId))

        case None => couldNotFindGallery(galleryId)
      }
    }
  }

  /** A gallery has to "go down" in the hierarchy of galleries */
  def down(galleryId: Int) = SecuredAction(WithRole(Role.Writer)).async { implicit request =>
    val categoryId = GalleryReadService.findCategoryId(galleryId)
    findAllFuture(categoryId).map { galleries =>
      galleries.find(_.galleryId == galleryId) match {
        case Some(gallery) =>
          val galleryRank = gallery.rank
          val authId = BackEndUser.user(request).authId

          // List is sorted by rank, thus we pick up the first gallery whose rank is < gallery's rank
          galleries.find(_.rank < galleryRank).map { galleryUnderneath =>
            GalleryWriteService.updateField(galleryId, "rank", Json.toJson(galleryRank - 1), authId)
            GalleryWriteService.updateField(galleryUnderneath.galleryId, "rank", Json.toJson(galleryRank), authId)
          }

          Redirect(routes.Galleries.galleries(categoryId))

        case _ => couldNotFindGallery(galleryId)
      }
    }
  }

  def onOffLine(galleryId: Int) = SecuredAction(WithRole(Role.Writer)).async { implicit request =>
    GalleryReadService.findById(galleryId).map {
      case Some(gallery) =>
        GalleryWriteService.updateField(galleryId, "online", Json.toJson(!gallery.online), BackEndUser.user(request).authId)
        Redirect(routes.Galleries.galleries(gallery.categoryId))
      case None =>
        couldNotFindGallery(galleryId)
    }
  }

  def findAll(categoryId: Int): List[Gallery] =
    Await.result(GalleryReadService.findAll(categoryId), 5 seconds)

  def findAllFuture(categoryId: Int): Future[List[Gallery]] =
    GalleryReadService.findAll(categoryId)

  def couldNotFindGallery(galleryId: Int): SimpleResult =
    BadRequest(views.html.global.badRequest("Could not find a gallery with ID " + galleryId))
}
