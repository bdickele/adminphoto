package app.models.category

import org.specs2.mutable.Specification
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import app.TestApplication
import service.CategoryService
import models.Category


class CategoryServiceSpec extends Specification {

  "Seach of a single category" should {

    lazy val future = CategoryService.find(1)
    lazy val category = Await.result(future, Duration(5, TimeUnit.SECONDS)).get

    "return 2004 when categoryID is 1" in new TestApplication {
      category.categoryId must equalTo(1)
      category.title must equalTo("2004")
      category.comment must equalTo("Année 2004")
      category.online must beTrue
    }
  }

  "Total list of categories" should {

    lazy val future = CategoryService.findAll
    lazy val list: List[Category] = Await.result(future, Duration(5, TimeUnit.SECONDS))

    "contain all categories" in new TestApplication {
      list.foreach(c => println("> Found " + c.toString))
      list.size must equalTo(10)
    }

    "contain 2004 as last category" in new TestApplication {
      val c = list.last
      c.categoryId must equalTo(1)
      c.title must equalTo("2004")
      c.comment must equalTo("Année 2004")
      c.online must beTrue
    }
  }

}
