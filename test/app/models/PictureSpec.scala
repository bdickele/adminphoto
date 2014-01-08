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
      val actual = mainFolders
      actual must equalTo(List("2013", "2012", "2011", "2010", "2009", "2008", "2007", "2006", "2005", "2004"))
    }
  }

  "The list of sub-folders for 2004" should {
    "contains 2 galleries : misc and venise" in {
      val subFolderNames = Picture.subFolders(mainFolders.last)
      subFolderNames must equalTo(List("0406_misc", "0408_venise"))
    }
  }


}
