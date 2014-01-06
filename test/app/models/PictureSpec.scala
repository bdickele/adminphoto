package app.models

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.Picture

/**
 * User: bdickele
 * Date: 1/6/14
 */
class PictureSpec extends Specification {

  val mainFolders = Picture.mainFolders

  "The list of categories" should {
    "contains references from 2004 to 2013" in {
      val actual = mainFolders.map(f => f.name).toList
      actual must equalTo(List("2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013"))
    }

    "contains 2004 with 2 galleries : misc and venise" in {
      val first = mainFolders.head

      first.name must equalTo("2004")
      first.folders must equalTo(List("0406_misc", "0408_venise"))
    }
  }


}
