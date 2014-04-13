package util

import play.api.Play

/**
 * Constants
 * bdickele
 */
object Const {

  lazy val OffLine = Play.current.configuration.getBoolean("offline").get

  // That val is the prefix to add to pictures in the html code, so that we have the complete URL
  lazy val PhotoStockRoot = Play.current.configuration.getString(
    if(OffLine) "photostock.root.url.local" else "photostock.root.url.remote").get

  val FolderWeb = "web/"
  val FolderThumbnail = "thumbnail/"
  val FolderPrint = "print/"

}
