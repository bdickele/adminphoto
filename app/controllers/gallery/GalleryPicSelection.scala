package controllers.gallery

import play.api.mvc.{Action, Controller}
import models.picture.{Picture, Folder}
import play.api.data.Forms._
import play.api.data.Form
import util.Const._
import models.gallery.{GalleryRW, GalleryPicturesRW, GalleryPic}
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Created by bdickele
 * Date: 2/2/14
 */

case class SelectedPics(galleryId: Int,
                        folder: String,
                        pictures: List[String])

case class SelectablePic(folder: String,
                         thumbnailComplete: String,
                         webComplete: String,
                         web: String)

object GalleryPicSelection extends Controller {

  // ---------------------------------------------------------------
  // Mapping with all rules to check + Form[Mapping[CategoryForm]]
  // ---------------------------------------------------------------
  val formMapping = mapping(
    "galleryId" -> number,
    "folder" -> text,
    "pictures" -> list(text))(SelectedPics.apply)(SelectedPics.unapply)

  val form: Form[SelectedPics] = Form(formMapping)


  def view(galleryId: Int, mainFolder: String = "", subFolder: String = "") = Action.async {
    val mainFolders = Folder.mainFolders
    val mainFolderName = if (mainFolder == "") mainFolders.head else mainFolder

    val subFolders = Folder.subFolders(mainFolderName)
    val subFolderName = if (subFolder == "") subFolders.head else subFolder

    val folder = mainFolderName + "/" + subFolderName + "/"
    val picturesRaw: List[Picture] = Picture.picturesFromFolder(folder)

    val selectablePics: List[SelectablePic] = picturesRaw.map(p =>
      SelectablePic(
        folder,
        WebRoot + folder + FolderThumbnail + p.thumbnail,
        WebRoot + folder + FolderWeb + p.web,
        p.web))

    val future = GalleryRW.findById(galleryId)
    future.map {
      option =>
        option match {
          case None => BadRequest("Com'on, that was not supposed to happen, really")
          case Some(gallery) => Ok(views.html.gallery.galleryPicSelection(form.fill(SelectedPics(galleryId, folder, List())),
            gallery.categoryId, galleryId, gallery.extendedTitle,
            mainFolders, subFolders, mainFolderName, subFolderName,
            selectablePics))
        }
    }
  }

  def save() = Action {
    implicit request =>
      form.bindFromRequest.fold(

        // Validation error
        formWithErrors => BadRequest("But that was not supposed to happen !"),

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
            GalleryPic("", // We don't care about the complete path to thumbnail here
              folder + FolderThumbnail + pic.thumbnail,
              folder + FolderWeb + pic.web,
              pic.print match {
                case None => None
                case Some(p) => Some(folder + FolderPrint + p)
              },
              None))
          GalleryPicturesRW.addPictures(form.galleryId, galleryPics)
          Redirect(routes.GalleryPicList.view(form.galleryId))
        })
  }
}
