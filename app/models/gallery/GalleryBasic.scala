package models.gallery

import reactivemongo.bson._
import reactivemongo.bson.BSONBoolean
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONInteger

/**
 * User: bdickele
 * Date: 1/13/14
 */
case class GalleryBasic(sectionId: Int,
                        galleryId: String,
                        title: String,
                        nbPictures: Int,
                        online: Boolean)

object GalleryBasic {

  implicit object GalleryBasicBSONHandler extends BSONDocumentReader[GalleryBasic] {

    def read(doc: BSONDocument): GalleryBasic =
      GalleryBasic(
        doc.getAs[BSONInteger]("categoryId").get.value,
        doc.getAs[BSONString]("galleryId").get.value,
        doc.getAs[BSONString]("title").get.value,
        doc.getAs[BSONArray]("pictures").get.length,
        doc.getAs[BSONBoolean]("online").get.value)
  }
}
