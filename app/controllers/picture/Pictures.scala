package controllers.picture

import play.api.mvc.{Action, Controller}
import models.picture.{Folder, PictureVO}

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

    Ok(views.html.picture.picture(mainFolders, subFolders, mainFolderName, subFolderName,
      PictureVO.pictures(mainFolderName, subFolderName)))
  }

}
