package app.controllers.gallery

import org.specs2.mutable.Specification
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import app.models.TestApplication
import controllers.gallery.Galleries
import models.gallery.GalleryBasic

/**
 * User: bdickele
 * Date: 1/14/14
 */
class GalleriesSpec extends Specification {
    /*

  "Total list of galleries-basic" should {

    lazy val listFuture: Future[List[GalleryBasic]] = Galleries.findAllBasic(1)
    lazy val list: List[GalleryBasic] = listFuture.value.get.get

    def waitForFuture(): Unit = Await.result(listFuture, Duration(1, TimeUnit.SECONDS))

    "contain all galleries of category 2004" in new TestApplication {
      waitForFuture()
      list.foreach(c => println("> Found " + c.toString))
      list.size must equalTo(1)
    }

    "contain 2004 as last category" in new TestApplication {
      waitForFuture()
      val g = list.head
      g.categoryId must equalTo(1)
      g.galleryId must equalTo("200406")
      //TODO test sur la date

      g.title must equalTo("Eté 2004: divers")
      g.online must beTrue
      g.nbPictures must equalTo(2)
    }
  }
    */
}
