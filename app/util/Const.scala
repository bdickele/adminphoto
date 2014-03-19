package util

import play.api.Play

/**
 * Created by bdickele on 17/01/14.
 */
object Const {

  lazy val LocalRoot = Play.current.configuration.getString("local.photostock.root").get
  //val WebRoot = "http://www.dickele.com/photostock/"
  //val WebRoot = "http://localhost:8080/photostock/"
  val WebRoot = "http://dickele.cluster010.ovh.net/photostock/"

  val FolderWeb = "web/"
  val FolderThumbnail = "thumbnail/"
  val FolderPrint = "print/"

}
