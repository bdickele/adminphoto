package app.models.gallery

import org.specs2.mutable.Specification
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import app.models.TestApplication
import models.gallery.{GalleryBasicRW, GalleryBasic}
import org.joda.time.YearMonth

/**
 * User: bdickele
 * Date: 1/14/14
 */
class GalleryBasicRWSpec extends Specification {

  "Total list of galleries (basic)" should {

    lazy val future = GalleryBasicRW.findAll(1)
    lazy val list: List[GalleryBasic] = Await.result(future, Duration(5, TimeUnit.SECONDS))

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
}
