package models.gallery

import reactivemongo.bson._
import models.util.Const
import reactivemongo.bson.BSONString

/**
 * Created by bdickele
 * Date: 29/01/14
 */
case class GalleryPics(galleryId: Int,
                       galleryTitle: String,
                       thumbnail: String,
                       pictures: List[GalleryPic])

case class GalleryPic(thumbnail: String,
                      web: String,
                      print: Option[String],
                      description: Option[String])

object GalleryPics {


  implicit object GalleryPicsBSONHandler extends BSONDocumentReader[GalleryPics] {

    def read(doc: BSONDocument): GalleryPics = {

      def readPicture(doc: BSONDocument): GalleryPic =
        GalleryPic(
          Const.WebRoot + doc.getAs[BSONString]("thumbnail").get.value,
          Const.WebRoot + doc.getAs[BSONString]("web").get.value,
          doc.getAs[BSONString]("print") match {
            case None => None
            case Some(s) => Some(Const.WebRoot + s.value)
          },
          doc.getAs[BSONString]("description") match {
            case None => None
            case Some(s) => Some(s.value)
          })

      def readPictures(array: BSONArray): List[GalleryPic] = {
        val stream: Stream[BSONValue] = array.values
        stream.toList.map(value => readPicture(value.asInstanceOf[BSONDocument]))
      }

      GalleryPics(
        doc.getAs[BSONInteger]("galleryId").get.value,
        doc.getAs[BSONString]("title").get.value,
        doc.getAs[BSONString]("thumbnail").get.value,
        readPictures(doc.getAs[BSONArray]("pictures").get))
    }
  }

}
