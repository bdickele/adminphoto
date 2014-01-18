package controllers.picture

import play.api.mvc.{Action, Controller}
import models.picture.Picture

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Pictures extends Controller {

  def refresh() = {
    Picture.clearCache()
    view()
  }

  def view(mainFolder: String = "", subFolder: String = "") = Action {
    val mainFolders = Picture.mainFolders
    val mainFolderName = if (mainFolder == "") mainFolders.head else mainFolder

    val subFolders = Picture.subFolders(mainFolderName)
    val subFolderName = if (subFolder == "") subFolders.head else subFolder

    Ok(views.html.picture.picture(mainFolders, subFolders, mainFolderName, subFolderName,
      Picture.pictures(mainFolderName, subFolderName)))
  }

}
