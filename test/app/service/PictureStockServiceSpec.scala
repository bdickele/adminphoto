package app.service

import org.specs2.mutable._

import app.TestApplication
import service.PictureStockService
import models.Picture

/**
 * User: bdickele
 * Date: 1/6/14
 */
class PictureStockServiceSpec extends Specification {

  val Url = "http://www.dickele.com/photostock/"

  lazy val mainFolders = PictureStockService.mainFolders


  "The list of categories" should {
    "contain references from 2004 to 2013" in new TestApplication {
        mainFolders must equalTo(List("2013", "2012", "2011", "2010", "2009", "2008", "2007", "2006", "2005", "2004"))
    }
  }

  "The list of sub-folders for 2004" should {
    "contain 2 galleries : misc and venise" in new TestApplication {
      val subFolderNames = PictureStockService.subFolders(mainFolders.last)
      subFolderNames must equalTo(List("0408_venise", "0406_misc"))
    }
  }

  "The list of picture for 2004/0406_misc" should {
    "contain 11 elements" in new TestApplication {
      val pictures = Picture.picturesFromFolder("2004/0406_misc/")
      pictures.size must equalTo(11)
    }
  }

  "First picture of gallery 2004/0406_misc" should {
    lazy val pictures = Picture.picturesFromFolder("2004/0406_misc/")
    lazy val first = pictures.head

    "have a thumbnail name equal to small_0406_Adrien.jpg" in new TestApplication {
      first.thumbnail must equalTo("small_0406_Adrien.jpg")
    }

    "have a web name equal to 0406_Adrien.jpg" in {
      first.web must equalTo("0406_Adrien.jpg")
    }

    "have no print version" in {
      first.print must equalTo(None)
    }
  }

  "First picture of gallery 2013/1312_1" should {
    lazy val pictures = Picture.picturesFromFolder("2013/1312_1/")
    lazy val first = pictures.head

    "have a thumbnail name equal to 201312_bdi_mumbai_002.jpg" in new TestApplication {
      first.thumbnail must equalTo("201312_bdi_mumbai_002.jpg")
    }

    "have a web name equal to 201312_bdi_mumbai_002.jpg" in {
      first.web must equalTo("201312_bdi_mumbai_002.jpg")
    }

    "have a print version equal to 201312_bdi_mumbai_002.jpg" in {
      first.print must equalTo(Some("201312_bdi_mumbai_002.jpg"))
    }
  }
}
