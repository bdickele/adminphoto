package app.controllers.category

import org.specs2.mutable.Specification
import scala.concurrent.{Await, Future}
import models.category.Category
import app.models.TestApplication
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import models.util.Access
import controllers.category.Categories


/**
 * User: bdickele
 * Date: 1/11/14
 */
class CategoriesSpec extends Specification {

  "Total list of categories" should {

    lazy val listFuture: Future[List[Category]] = Categories.findAll
    lazy val list: List[Category] = listFuture.value.get.get

    def waitForFuture(): Unit = Await.result(listFuture, Duration(1, TimeUnit.SECONDS))

    "contain all categories" in new TestApplication {
      waitForFuture()
      list.foreach(c => println("> Found " + c.toString))
      list.size must equalTo(2)
    }

    "contain 2004 as last category" in new TestApplication {
      waitForFuture()
      val c = list.last
      c.categoryId must equalTo(1)
      c.title must equalTo("2004 test")
      c.description must equalTo("Année 2004")
      c.online must beTrue
      c.access must equalTo(Access.Guest)
    }
  }

}
