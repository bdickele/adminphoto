package app.service

import org.specs2.mutable.Specification
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import app.TestApplication
import service.CategoryService
import models.Category
import play.api.libs.json.JsObject
import reactivemongo.bson.{BSONValue, BSONDocument}


class CategoryServiceSpec extends Specification {

  "Method find(categoryId)" should {

    lazy val future = CategoryService.find(1)
    lazy val category = Await.result(future, Duration(5, TimeUnit.SECONDS)).get

    "return 2004 when categoryID is 1" in new TestApplication {
      category.categoryId must equalTo(1)
      category.title must equalTo("2004")
      category.comment must equalTo(None)
      category.online must beTrue
    }
  }

  "Method findAll()" should {

    lazy val future = CategoryService.findAll
    lazy val list: List[Category] = Await.result(future, Duration(5, TimeUnit.SECONDS))

    "contain all categories" in new TestApplication {
      //list.foreach(c => println("> Found " + c.toString))
      list.size must equalTo(11)
    }

    "contain 2004 as last category" in new TestApplication {
      val c = list.last
      c.categoryId must equalTo(1)
      c.title must equalTo("2004")
    }
  }

  "Method findAllJson()" should {

    lazy val future = CategoryService.findAllJson
    lazy val list: List[JsObject] = Await.result(future, Duration(5, TimeUnit.SECONDS))

    "contain all categories" in new TestApplication {
      //list.foreach(c => println("> JsObject found " + c.toString))
      list.size must equalTo(11)
    }
  }

  "Method findNumberOfGalleryByCategory" should {

    "return number of galleries by category" in new TestApplication {
      val future = CategoryService.findNumberOfGalleryByCategory()

      val list: List[JsObject] = Await.result(future, Duration(5, TimeUnit.SECONDS))

      val map: Map[Int, Int] = list.map { jsObj =>
        ((jsObj \ "_id").as[Int] -> (jsObj \ "number").as[Int])
      }.toMap

      //println(map)

      // 2 galleries for 2004
      var nb = map.get(1).get
      nb must equalTo(2)

      // 2 galleries for 2005
      nb = map.get(2).get
      nb must equalTo(8)
    }
  }

  "Methods create" should {

    "just work" in new TestApplication {
      val future = CategoryService.create("Test gallery", Some("Test"), online = false)
      val lastError = Await.result(future, Duration(5, TimeUnit.SECONDS))

      lastError.ok must equalTo(true)

      // Checking that last category is the one we just created (so that we can safely delete it)
      val categories: List[Category] = Await.result(CategoryService.findAll, Duration(5, TimeUnit.SECONDS))
      val maxCategoryId = categories.maxBy(_.categoryId).categoryId
      lazy val category = Await.result(CategoryService.find(maxCategoryId), Duration(5, TimeUnit.SECONDS)).get

      category.title must equalTo("Test gallery")

      val futureDelete = CategoryService.delete(maxCategoryId)
      val lastErrorDelete = Await.result(futureDelete, Duration(5, TimeUnit.SECONDS))

      lastErrorDelete.ok must equalTo(true)
    }
  }

  "Method delete" should {

    "should throw an error when category is not empty" in new TestApplication {
      val future = CategoryService.delete(1)
      val lastError = Await.result(future, Duration(5, TimeUnit.SECONDS))

      lastError.ok must equalTo(false)
    }
  }

}
