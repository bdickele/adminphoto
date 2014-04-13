package app.service

import org.specs2.mutable._

import service.UserService


class UserServiceSpec extends Specification {

  "When creating a new authId (with a list of existing authIds) it" should {

    "create appropriate prefix" in {

      val result1 = UserService.buildAuthIdPrefix("DICKELE", "Bertrand")
      result1 must equalTo("dicb_")

      val result2 = UserService.buildAuthIdPrefix("DI", "Bertrand")
      result2 must equalTo("dib_")
    }

    "return an authId with appropriate suffix" in {

      val result1 = UserService.createNewAuthId("dicb_", List())
      result1 must equalTo("dicb_1")

      val result2 = UserService.createNewAuthId("dicb_", List("dicb_1", "dicb_4", "dicb_2"))
      result2 must equalTo("dicb_5")
    }
  }
}
