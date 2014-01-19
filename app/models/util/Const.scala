package models.util

import play.api.Play

/**
 * Created by bdickele on 17/01/14.
 */
object Const {

  lazy val LocalRoot = Play.current.configuration.getString("local.photostock.root").get
  val WebRoot = "http://www.dickele.com/photostock/"

}
