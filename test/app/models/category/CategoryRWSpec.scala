package app.models.category

import org.specs2.mutable.Specification
import models.category.{CategoryRW, Category}
import app.models.TestApplication
import models.util.Access
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit


/**
 * User: bdickele
 * Date: 1/11/14
 */
class CategoryRWSpec extends Specification {

  "Total list of categories" should {

    lazy val future = CategoryRW.findAll
    lazy val list: List[Category] = Await.result(future, Duration(5, TimeUnit.SECONDS))

    "contain all categories" in new TestApplication {
      list.foreach(c => println("> Found " + c.toString))
      list.size must equalTo(2)
    }

    "contain 2004 as last category" in new TestApplication {
      val c = list.last
      c.categoryId must equalTo(1)
      c.title must equalTo("2004")
      c.description must equalTo("Année 2004")
      c.online must beTrue
      c.access must equalTo(Access.Guest)
    }
  }

}
