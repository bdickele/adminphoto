package models.gallery

import reactivemongo.bson._
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONInteger

/**
 * User: bdickele
 * Date: 1/13/14
 */

case class CategoryAndGallery(categoryId: Int,
                              categoryTitle: String,
                              galleries: List[GalleryBasic])

case class GalleryBasic(galleryId: String,
                        title: String,
                        thumbnail: String,
                        nbPictures: Int,
                        online: Boolean)

object CategoryAndGallery {

  implicit object CategoryAndGalleryHandler extends BSONDocumentReader[CategoryAndGallery] {

    def read(doc: BSONDocument): CategoryAndGallery = {
      CategoryAndGallery(
        doc.getAs[BSONInteger]("categoryId").get.value,
        doc.getAs[BSONString]("categoryTitle").get.value,
        readGalleries(doc.getAs[BSONArray]("galleries").get.values))
    }

    def readGalleries(stream: Stream[BSONValue]): List[GalleryBasic] = {
      println("readGalleries")
      stream.foreach(v => println(v))
      List()
    }


    /*doc.getAs[BSONString]("title").get.value,
        doc.getAs[BSONString]("galleryId").get.value,
    doc.getAs[BSONString]("thumbnail").get.value,
    doc.getAs[BSONArray]("pictures").get.length,
    doc.getAs[BSONBoolean]("online").get.value)
    */
  }

}
