package controllers.gallery

import play.api.mvc.{SimpleResult, Controller}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import controllers.category.Categories
import securesocial.core.SecureSocial
import service.{GalleryReadService, GalleryWriteService}
import play.api.libs.json.Json
import models.{Role, WithRole, Gallery, Category}

/**
 * Controller for galleries
 * bdickele
 */
object Galleries extends Controller with SecureSocial {

  def view(passedCategoryId: Int = -1) = SecuredAction.async {
    implicit request =>
      val categories: List[Category] = Categories.findAllFromCacheOrDB()
      val categoryId = if (passedCategoryId > 0) passedCategoryId else categories.head.categoryId

      // In case URL contains an incorrect gallery ID
      categories.exists(c => c.categoryId == categoryId) match {
        case false => Future.successful(Categories.couldNotFindCategory(categoryId))

        case true => GalleryReadService.findAll(categoryId).map {
          galleries => Ok(views.html.gallery.gallery(categoryId, categories, galleries))
        }.recover {
          case e =>
            Logger.error(e.getMessage)
            BadRequest(views.html.badRequest(e.getMessage))
        }
      }
  }

  def refresh(categoryId: Int) = SecuredAction {
    implicit request =>
      Redirect(routes.Galleries.view(categoryId))
  }

  /** A gallery has to "go up" in the hierarchy of galleries */
  def up(categoryId: Int, galleryId: Int) = SecuredAction(WithRole(Role.Writer)).async {
    implicit request =>
      findAllFuture(categoryId).map {
        galleries =>
          galleries.find(_.galleryId == galleryId) match {
            case Some(gallery) =>
              val galleryRank = gallery.rank

              // List is sorted by rank: we reverse it and pick up the first category whose rank is > category's rank
              galleries.reverse.find(_.rank > galleryRank) match {
                case Some(otherGallery) =>
                  GalleryWriteService.updateField(galleryId, "rank", Json.toJson(galleryRank + 1))
                  GalleryWriteService.updateField(otherGallery.galleryId, "rank", Json.toJson(galleryRank))
                case _ => // nothing to do then
              }

              Redirect(routes.Galleries.view(categoryId))

            case None => couldNotFindGallery(galleryId)
          }
      }
  }

  /** A gallery has to "go down" in the hierarchy of galleries */
  def down(categoryId: Int, galleryId: Int) = SecuredAction(WithRole(Role.Writer)).async {
    implicit request =>
      findAllFuture(categoryId).map {
        galleries =>
          galleries.find(_.galleryId == galleryId) match {
            case Some(gallery) =>
              val galleryRank = gallery.rank

              // List is sorted by rank, thus we pick up the first gallery whose rank is < gallery's rank
              galleries.find(_.rank < galleryRank) match {
                case Some(otherGallery) =>
                  GalleryWriteService.updateField(galleryId, "rank", Json.toJson(galleryRank - 1))
                  GalleryWriteService.updateField(otherGallery.galleryId, "rank", Json.toJson(galleryRank))
                case _ => // nothing to do then
              }

              Redirect(routes.Galleries.view(categoryId))

            case _ => couldNotFindGallery(galleryId)
          }
      }
  }

  def onOffLine(galleryId: Int) = SecuredAction(WithRole(Role.Writer)).async {
    implicit request =>
      GalleryReadService.findById(galleryId).map {
        _ match {
          case Some(gallery) =>
            GalleryWriteService.updateField(galleryId, "online", Json.toJson(!gallery.online))
            Redirect(routes.Galleries.view(gallery.categoryId))
          case None =>
            couldNotFindGallery(galleryId)
        }
      }
  }

  def findAll(categoryId: Int): List[Gallery] =
    Await.result(GalleryReadService.findAll(categoryId), 5 seconds)

  def findAllFuture(categoryId: Int): Future[List[Gallery]] =
    GalleryReadService.findAll(categoryId)

  def couldNotFindGallery(galleryId: Int): SimpleResult =
    BadRequest(views.html.badRequest("Could not find a gallery with ID " + galleryId))

  def notAllowed: SimpleResult =
    BadRequest(views.html.badRequest("You're not allowed to perform this operation"))
}
