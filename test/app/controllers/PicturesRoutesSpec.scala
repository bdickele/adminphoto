package app.controllers

import org.specs2.mutable.Specification
import app.TestApplication
import play.api.test.Helpers._

/**
 * Created by bdickele
 * Date: 21/04/14
 */
class PicturesRoutesSpec extends Specification {

  "routes related to pictures" should {

    "return 200 (OK) for correct URLs" in new TestApplication {

      var response = responseURL("picture")
      response.status must equalTo(OK)

      response = responseURL("picture/refresh")
      response.status must equalTo(OK)

      response = responseURL("picture/2004")
      response.status must equalTo(OK)

      response = responseURL("picture/2004/0406")
      response.status must equalTo(OK)
    }

    "return 404 (Page not found) for incorrect URL" in new TestApplication {
      val response = responseURL("pictur")
      response.status must equalTo(NOT_FOUND)
    }
  }
}
