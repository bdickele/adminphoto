package app.models.util

import org.specs2.mutable.Specification
import models.util.Access
import java.util.NoSuchElementException

/**
 * User: bdickele
 * Date: 1/11/14
 */
class AccessSpec extends Specification {

  "Method Access.fromString" should {
    "return Guest when passed String is G" in {
      Access.fromString("G") must equalTo(Access.Guest)
    }

    "return User when passed String is U" in {
      Access.fromString("U") must equalTo(Access.User)
    }

    "throw a NoSuchElementException when passing a non valid String" in {
      Access.fromString("Z") must throwA[NoSuchElementException]
    }
  }
}
