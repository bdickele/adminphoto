package util

import play.api.Play

/**
 * Created by bdickele on 17/01/14.
 */
object Const {

  val LocalRoot = Play.current.configuration.getString("local.photostock.root").get
  val WebRoot = Play.current.configuration.getString("web.photostock.root").get

  val FolderWeb = "web/"
  val FolderThumbnail = "thumbnail/"
  val FolderPrint = "print/"

}
