package controllers.picture

import play.api.mvc.{AnyContent, Action, Controller}
import models.picture.Picture

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Pictures extends Controller {

  def refreshFolders() = {
    Picture.clearCache()
    pictures0
  }

  def pictures0: Action[AnyContent] = pictures(None, None)

  def pictures1(mainFolder: String): Action[AnyContent] = pictures(Some(mainFolder), None)

  def pictures2(mainFolder: String, subFolder: String): Action[AnyContent] = pictures(Some(mainFolder), Some(subFolder))

  def pictures(mainFolder: Option[String] = None, subFolder: Option[String] = None) = Action {
    val mainFolders = Picture.mainFolders

    val mainFolderName = mainFolder match {
      case Some(name) => name
      case None => mainFolders.head
    }

    val subFolders = Picture.subFolders(mainFolderName)

    val subFolderName = subFolder match {
      case Some(name) => name
      case None => subFolders.head
    }

    Ok(views.html.picture.picture(mainFolders, subFolders, mainFolderName, subFolderName,
      Picture.pictures(mainFolderName, subFolderName)))
  }

}