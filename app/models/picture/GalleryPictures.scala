package models.picture

import reactivemongo.bson._
import models.util.Const
import reactivemongo.bson.BSONString

/**
 * Created by bdickele
 * Date: 29/01/14
 */

case class GalleryPictures(galleryId: Int,
                           pictures: List[GalleryPicture])

case class GalleryPicture(thumbnail: String,
                          web: String,
                          print: Option[String],
                          description: Option[String])

object GalleryPictures {


  implicit object GalleryPicturesBSONHandler extends BSONDocumentReader[GalleryPictures] {

    def read(doc: BSONDocument): GalleryPictures = {

      def readPicture(doc: BSONDocument): GalleryPicture =
        GalleryPicture(
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

      def readPictures(array: BSONArray): List[GalleryPicture] = {
        val stream: Stream[BSONValue] = array.values
        stream.toList.map(value => readPicture(value.asInstanceOf[BSONDocument]))
      }

      GalleryPictures(
        doc.getAs[BSONInteger]("galleryId").get.value,
        readPictures(doc.getAs[BSONArray]("pictures").get))
    }
  }

}
