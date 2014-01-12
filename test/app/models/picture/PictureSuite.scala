package app.models.picture

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import models.picture.Picture


/**
 * User: bdickele
 * Date: 1/6/14
 */
@RunWith(classOf[JUnitRunner])
class PictureSuite extends FunSuite {

  test("list of categories") {
    val expected = List("2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013")
    val actual = Picture.mainFolders
    assert(actual === expected)
  }

}
