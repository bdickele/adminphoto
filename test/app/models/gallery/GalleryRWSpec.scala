package app.models.gallery

import org.specs2.mutable.Specification
import models.gallery.{GalleryRW, Gallery}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import app.models.TestApplication
import org.joda.time.YearMonth
import models.util.Access

/**
 * Created by bdickele
 * Date: 29/01/14
 */
class GalleryRWSpec extends Specification {

  "Method findById" should {

    lazy val future = GalleryRW.findById(1)
    lazy val g: Gallery = Await.result(future, Duration(5, TimeUnit.SECONDS)).get

    "return very first gallery when passing 1" in new TestApplication {

      g.categoryId must equalTo(1)
      g.galleryId must equalTo(1)
      g.rank must equalTo(0)
      g.date must equalTo(new YearMonth(2004, 6))
      g.title must equalTo("Eté 2004: divers")
      g.online must beTrue
      g.access must equalTo(Access.Guest)
    }
  }
}