package util

import play.api.Play

/**
 * Constants
 * bdickele
 */
object Const {

  val LocalRoot = Play.current.configuration.getString("local.photostock.root").get
  val WebRoot = Play.current.configuration.getString("web.photostock.root").get

  val FolderWeb = "web/"
  val FolderThumbnail = "thumbnail/"
  val FolderPrint = "print/"

}
