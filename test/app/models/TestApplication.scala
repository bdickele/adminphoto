package app.models

import org.specs2.execute.{AsResult, Result}
import play.api.test.{FakeApplication, WithApplication}

/**
 * User: bdickele
 * Date: 1/12/14
 */
abstract class TestApplication extends WithApplication(
  FakeApplication(additionalConfiguration =
    Map(
      "mongodb.servers" -> List("localhost:27017"),
      "mongodb.db" -> "websitetest"))) {

  override def around[T: AsResult](test: => T): Result = super.around {
    setupData()
    test
  }

  def setupData() {
    // setup data
  }
}