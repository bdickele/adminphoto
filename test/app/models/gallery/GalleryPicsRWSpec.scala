package app.models.gallery

import org.specs2.mutable.Specification
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import models.gallery.GalleryPics
import util.Const
import app.TestApplication
import service.GalleryPicturesRW

/**
 * Created by bdickele
 * Date: 29/01/14
 */

class GalleryPicsRWSpec extends Specification {

  "Method findByGalleryId" should {

    lazy val future = GalleryPicturesRW.findByGalleryId(1)
    lazy val galleryPictures: GalleryPics = Await.result(future, Duration(5, TimeUnit.SECONDS)).get

    "return pictures of the gallery" in new TestApplication {
      galleryPictures.galleryId must equalTo(1)

      val pictures = galleryPictures.pictures
      pictures.length must equalTo(2)

      val first = pictures.head
      first.thumbnail must equalTo(Const.WebRoot + "2004/0406_misc/small/small_0406_Adrien.jpg")
      first.web must equalTo(Const.WebRoot + "2004/0406_misc/web/0406_Adrien.jpg")
      first.print must equalTo(None)
      first.comment must equalTo(Some("Avec mon fréro"))

      val second = pictures.tail.head
      second.thumbnail must equalTo(Const.WebRoot + "2004/0406_misc/small/small_0406_Bastille.jpg")
      second.web must equalTo(Const.WebRoot + "2004/0406_misc/web/0406_Bastille.jpg")
      second.print must equalTo(None)
      second.comment must equalTo(None)
    }
  }
}