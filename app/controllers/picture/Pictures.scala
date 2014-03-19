package controllers.picture

import play.api.mvc.{Action, Controller}
import models.picture.{Picture, Folder}
import util.Const._


/**
 * User: bdickele
 * Date: 1/7/14
 */
object Pictures extends Controller {

  def refresh() = Action {
    Folder.clearCache()
    Redirect(routes.Pictures.view("", ""))
  }

  def view(mainFolder: String = "", subFolder: String = "") = Action {
    val mainFolders = Folder.mainFolders
    val mainFolderName = if (mainFolder == "") mainFolders.head else mainFolder

    val subFolders = Folder.subFolders(mainFolderName)
    val subFolderName = if (subFolder == "") subFolders.head else subFolder

    val folder = mainFolderName + "/" + subFolderName + "/"
    val picturesRaw: List[Picture] = Picture.picturesFromFolder(folder)

    val pathThumbnailUrl = WebRoot + folder + FolderThumbnail
    val pathWebUrl = WebRoot + folder + FolderWeb

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
