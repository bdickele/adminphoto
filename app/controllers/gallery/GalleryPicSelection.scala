package controllers.gallery

import play.api.mvc.{Action, Controller}
import models.picture.{Picture, Folder}
import play.api.data.Forms._
import play.api.data.Form
import util.Const._

/**
 * Created by bdickele
 * Date: 2/2/14
 */

case class SelectedPics(galleryId: Int,
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
    "pictures" -> list(text))(SelectedPics.apply)(SelectedPics.unapply)

  val form: Form[SelectedPics] = Form(formMapping)


  def view(galleryId: Int, mainFolder: String = "", subFolder: String = "") = Action {
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

    Ok(views.html.gallery.galleryPicSelection(SelectedPics(galleryId, List()),
      galleryId, mainFolders, subFolders, mainFolderName, subFolderName,
      selectablePics))
  }

  def save() = Action {
    implicit request =>
      form.bindFromRequest.fold(

        // Validation error
        formWithErrors => BadRequest("But that's what not supposed to happen !"),

        //TODO
        form => {
          println("---- FORM ----")
          println(form.galleryId)
          println(form.pictures)
          form.pictures.map(
            println(_)
          )
          Redirect(routes.GalleryPicList.view(form.galleryId))
        })
  }
}
