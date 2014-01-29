package models.picture

import reactivemongo.bson._
import models.util.Const
import reactivemongo.bson.BSONString

/**
 * Created by bdickele
 * Date: 29/01/14
 */

case class GalleryPicture(thumbnail: String,
                          web: String,
                          print: Option[String],
                          description: Option[String])


object GalleryPicture {

  implicit object GalleryPictureBSONHandler extends BSONDocumentReader[GalleryPicture] {

    def read(doc: BSONDocument): GalleryPicture =
      GalleryPicture(
        Const.WebRoot + doc.getAs[BSONString]("thumbnail").get.value,
        Const.WebRoot + doc.getAs[BSONString]("web").get.value,
        doc.getAs[BSONString]("print") match {
          case None => None
          case Some(bsonString) => Some(Const.WebRoot + bsonString.value)
        },
        doc.getAs[BSONString]("description") match {
          case None => None
          case Some(bsonString) => Some(bsonString.value)
        })
  }

}
