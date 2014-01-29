package app.models.picture

import org.specs2.mutable.Specification
import app.models.TestApplication
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import models.picture.{GalleryPictureRW, GalleryPicture}
import models.util.Const

/**
 * Created by bdickele
 * Date: 29/01/14
 */

class GalleryPictureRWSpec extends Specification {

  "Method findByGalleryId" should {

    lazy val future = GalleryPictureRW.findByGalleryId(1)
    lazy val list: List[GalleryPicture] = Await.result(future, Duration(5, TimeUnit.SECONDS))

    "return pictures of the gallery" in new TestApplication {
      GalleryPictureRW.findByGalleryId(1)
      list.length must equalTo(2)

      val first = list.head
      first.thumbnail must equalTo(Const.WebRoot + "2004/0406_misc/small/small_0406_Adrien.jpg")
      first.web must equalTo(Const.WebRoot + "2004/0406_misc/web/0406_Adrien.jpg")
      first.description must equalTo(Some("Avec mon fréro"))
    }
  }
}