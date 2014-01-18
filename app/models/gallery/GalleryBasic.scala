package models.gallery

import reactivemongo.bson._
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONInteger
import models.util.{Const, Access}
import org.joda.time.YearMonth

/**
 * User: bdickele
 * Date: 1/13/14
 */

case class GalleryBasic(categoryId: Int,
                        rank: Int,
                        galleryId: Int,
                        title: String,
                        date: YearMonth,
                        thumbnail: String,
                        nbPictures: Int,
                        online: Boolean,
                        access: Access.Value)

object GalleryBasic {

  implicit object GalleryBasicBSONHandler extends BSONDocumentReader[GalleryBasic] {

    def read(doc: BSONDocument): GalleryBasic = {
      GalleryBasic(
        doc.getAs[BSONInteger]("categoryId").get.value,
        doc.getAs[BSONInteger]("rank").get.value,
        doc.getAs[BSONInteger]("galleryId").get.value,
        doc.getAs[BSONString]("title").get.value,
        Gallery.buildDate(doc.getAs[BSONString]("date").get.value),
        Const.WebRoot + doc.getAs[BSONString]("thumbnail").get.value,
        doc.getAs[BSONArray]("pictures").get.length,
        doc.getAs[BSONBoolean]("online").get.value,
        doc.getAs[BSONString]("access").map(s => Access.fromString(s.value)).get)
    }
  }

}
