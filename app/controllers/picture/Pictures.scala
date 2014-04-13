package controllers.picture

import play.api.mvc.{Action, Controller}
import util.Const._
import securesocial.core.SecureSocial
import service.PictureStockService
import models.Picture


/**
 * Controller for screen related to list of available pictures
 * bdickele
 */
object Pictures extends Controller with SecureSocial {

  def refresh() = Action { implicit request =>
    PictureStockService.clearCache()
    Redirect(routes.Pictures.view("", ""))
  }

  def view(mainFolder: String = "", subFolder: String = "") = SecuredAction { implicit request =>
    val mainFolders = PictureStockService.loadMainFolders
    val mainFolderName = if (mainFolder == "") mainFolders.head else mainFolder

    val subFolders = PictureStockService.loadSubFolders(mainFolderName)
    val subFolderName = if (subFolder == "") subFolders.head else subFolder

    val folder = mainFolderName + "/" + subFolderName + "/"
    val picturesRaw: List[Picture] = Picture.picturesFromFolder(folder)

    val pathThumbnailUrl = PhotoStockRoot + folder + FolderThumbnail
    val pathWebUrl = PhotoStockRoot + folder + FolderWeb

    val picturesVO: List[PictureVO] = picturesRaw.map(picture =>
      PictureVO(
        pathThumbnailUrl + picture.thumbnail,
        pathWebUrl + picture.web,
        picture.thumbnail,
        picture.web,
        picture.print))

    Ok(views.html.picture.picture(mainFolders, subFolders, mainFolderName, subFolderName, picturesVO))
  }

}

/**
 * <p>Paths for a picture to be displayed in the Pictures screen</p>
 * @param thumbnailComplete Complete path to thumbnail (mandatory)
 * @param webComplete Complete path to web version (mandatory)
 * @param thumbnail Short path to thumbnail (mandatory)
 * @param web Short path to web version (mandatory)
 * @param print Short path to print version (optional)
 */
case class PictureVO(thumbnailComplete: String,
                     webComplete: String,
                     thumbnail: String,
                     web: String,
                     print: Option[String])
