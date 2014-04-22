package app.controllers

import org.specs2.mutable.Specification
import app.TestApplication
import play.api.test.Helpers._

/**
 * Created by bdickele
 * Date: 21/04/14
 */
class CategoriesRoutesSpec extends Specification {

  "routes related to categories" should {

    "return 200 (OK) for correct URLs" in new TestApplication {

      var response = responseURL("category")
      response.status must equalTo(OK)

      response = responseURL("category/refresh")
      response.status must equalTo(OK)

      response = responseURL("category/create")
      response.status must equalTo(OK)

      response = responseURL("category/edit/1")
      response.status must equalTo(OK)

      response = responseURL("category/up/1")
      response.status must equalTo(OK)

      response = responseURL("category/down/1")
      response.status must equalTo(OK)

      response = responseURL("category/onOffLine/1")
      response.status must equalTo(OK)
    }

    "return 404 (Page not found) for incorrect URL" in new TestApplication {
      val response = responseURL("categor")
      response.status must equalTo(NOT_FOUND)
    }

  }

}
