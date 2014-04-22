package app.controllers

import app.TestApplication
import org.specs2.mutable._
import play.api.test.Helpers._


class GalleriesRoutesSpec extends Specification {

  "routes related to galleries" should {

    "return 200 (OK) for correct URLs" in new TestApplication {
      var response = responseURL("gallery")
      response.status must equalTo(OK)

      response = responseURL("gallery/create/1")
      response.status must equalTo(OK)

      response = responseURL("gallery/edit/1")
      response.status must equalTo(OK)

      response = responseURL("gallery/previous/1")
      response.status must equalTo(OK)

      response = responseURL("gallery/next/1")
      response.status must equalTo(OK)

      response = responseURL("gallery/1")
      response.status must equalTo(OK)

      response = responseURL("gallery/refresh/1")
      response.status must equalTo(OK)

      response = responseURL("gallery/up/1")
      response.status must equalTo(OK)

      response = responseURL("gallery/down/1")
      response.status must equalTo(OK)

      response = responseURL("gallery/onOffLine/1")
      response.status must equalTo(OK)

      response = responseURL("galleryPicList/view/1")
      response.status must equalTo(OK)

      response = responseURL("galleryPicList/select/1/0;1")
      response.status must equalTo(OK)

      response = responseURL("galleryPicList/thumbnail/1/0")
      response.status must equalTo(OK)

      response = responseURL("galleryPicComment/1/0")
      response.status must equalTo(OK)

      response = responseURL("galleryPicSelection/1")
      response.status must equalTo(OK)

      response = responseURL("galleryPicSelection/1/2004")
      response.status must equalTo(OK)

      response = responseURL("galleryPicSelection/1/2004/0604")
      response.status must equalTo(OK)
    }

    "return 404 (Page not found) for incorrect URL" in new TestApplication {
      val response = responseURL("galler")
      response.status must equalTo(NOT_FOUND)
    }

  }
}
