package controllers.gallery

import play.api.mvc.Controller
import play.api.data.Forms._
import play.api.data.Form
import util.Const._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Await
import scala.concurrent.duration._
import language.postfixOps
import securesocial.core.SecureSocial
import service.{PictureStockService, GalleryReadService, GalleryWriteService}
import models._
import models.WithRole
import scala.Some
import models.GalleryPic

/**
 * Controller related to screen where we select pictures for a gallery
 * bdickele
 */
case class SelectedPics(galleryId: Int,
                        folder: String,
                        pictures: List[String])

case class SelectablePic(folder: String,
                         thumbnailComplete: String,
                         webComplete: String,
                         web: String)

object GalleryPicSelection extends Controller with SecureSocial {

  // ---------------------------------------------------------------
  // Mapping with all rules to check + Form[Mapping[CategoryForm]]
  // ---------------------------------------------------------------
  val formMapping = mapping(
    "galleryId" -> number,
    "folder" -> text,
    "pictures" -> list(text))(SelectedPics.apply)(SelectedPics.unapply)

  val form: Form[SelectedPics] = Form(formMapping)


  def pictures(galleryId: Int, mainFolder: String = "", subFolder: String = "") = SecuredAction.async { implicit request =>
    val future = GalleryReadService.findById(galleryId)
    future.map {
      case None => BadRequest(views.html.global.badRequest("Com'on, that was not supposed to happen, really"))
      case Some(gallery) =>

        val mainFolders = PictureStockService.remoteMainFolders

        // Let's select main folder with same name as gallery's year if user hasn't selected any parentFolder
        val mainFolderName = if (mainFolder == "") mainFolders.head else mainFolder

        val subFolders = PictureStockService.remoteSubFolders(mainFolderName)
        val subFolderName = if (subFolder == "") subFolders.head else subFolder

        val folder = mainFolderName + "/" + subFolderName + "/"
        val picturesRaw: List[Picture] = Picture.picturesFromFolder(folder)

        val selectablePics: List[SelectablePic] = picturesRaw.map(p =>
          SelectablePic(
            folder,
            PhotoStockRoot + folder + FolderThumbnail + p.thumbnail,
            PhotoStockRoot + folder + FolderWeb + p.web,
            p.web))

        Ok(views.html.gallery.galleryPicSelection(
          form.fill(SelectedPics(galleryId, folder, List())),
          gallery,
          mainFolders, subFolders, mainFolderName, subFolderName,
          selectablePics))
    }
  }

  def save() = SecuredAction(WithRole(Role.Writer)) { implicit request =>
    form.bindFromRequest.fold(

      // Validation error
      formWithErrors => BadRequest(views.html.global.badRequest("But that was not supposed to happen !")),

      /*
      So now we have a list of String looking like "pictureName.jpg" that
      stand for pictures to add.
      First : let's use these Strings to build the values to add in the DB
      Then: Update the DB
       */
      form => {
        val folder = form.folder
        val picturesRaw = form.pictures

        val availablePics = Picture.picturesFromFolder(folder)
        val filteredPictures: List[Picture] = availablePics.filter(p => picturesRaw.contains(p.web))

        val galleryPics: List[GalleryPic] = filteredPictures.map(pic =>
          GalleryPic(
            folder + FolderThumbnail + pic.thumbnail,
            folder + FolderWeb + pic.web,
            pic.print match {
              case None => None
              case Some(p) => Some(folder + FolderPrint + p)
            },
            None))
        // Waiting for update otherwise screen could be displayed before being updated
        val future = GalleryWriteService.addPictures(form.galleryId, galleryPics, BackEndUser.user(request).authId)
        Await.result(future, 5 seconds)
        Redirect(routes.GalleryPicList.pictures(form.galleryId))
      })
  }
}
