package app.models.gallery

import org.specs2.mutable.Specification
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import org.joda.time.YearMonth
import app.TestApplication
import service.GalleryR
import models.Gallery

/**
 * Created by bdickele
 * Date: 29/01/14
 */
class GalleryRWSpec extends Specification {

  "Total list of galleries (basic)" should {

    lazy val future = GalleryR.findAll(1)
    lazy val list: List[Gallery] = Await.result(future, Duration(5, TimeUnit.SECONDS))

    "contain all galleries of category 2004" in new TestApplication {
      list.foreach(c => println("> Found " + c.toString))
      list.size must equalTo(1)
    }

    "contain 'Summer 2004' as first gallery" in new TestApplication {
      val g = list.head
      g.categoryId must equalTo(1)
      g.galleryId must equalTo(1)
      g.date must equalTo(new YearMonth(2004, 6))
      g.title must equalTo("Eté 2004: divers")
      g.online must beTrue
      g.nbPictures must equalTo(2)
    }
  }

  "Method findById" should {

    lazy val future = GalleryR.findById(1)
    lazy val g: Gallery = Await.result(future, Duration(5, TimeUnit.SECONDS)).get

    "return very first gallery when passing 1" in new TestApplication {

      g.categoryId must equalTo(1)
      g.galleryId must equalTo(1)
      g.rank must equalTo(0)
      g.date must equalTo(new YearMonth(2004, 6))
      g.title must equalTo("Eté 2004: divers")
      g.online must beTrue
      //g.access must equalTo(Access.Guest)
    }
  }


  "Method findByTitle" should {

    "return a gallery when title exists" in new TestApplication {
      val future = GalleryR.findByTitle("Eté 2004: divers")
      val gallery: Gallery = Await.result(future, Duration(5, TimeUnit.SECONDS)).get
      gallery.galleryId must equalTo(1)
      gallery.title must equalTo("Eté 2004: divers")
    }

    "return nothing when title doesn't exist" in new TestApplication {
      val future = GalleryR.findByTitle("blablabla")
      val gallery: Option[Gallery] = Await.result(future, Duration(5, TimeUnit.SECONDS))
      gallery must equalTo(None)
    }
  }
}