package models.gallery

import reactivemongo.bson._
import reactivemongo.bson.BSONString
import util.Const

/**
 * Created by bdickele
 * Date: 29/01/14
 */
case class GalleryPics(categoryId: Int,
                       galleryId: Int,
                       thumbnail: String,
                       pictures: List[GalleryPic])

case class GalleryPic(thumbnailComplete: String,
                      thumbnail: String,
                      web: String,
                      print: Option[String],
                      comment: Option[String])

object GalleryPics {

  implicit object GalleryPicBSONHandler extends BSONDocumentWriter[GalleryPic] {

    def write(pic: GalleryPic): BSONDocument =
      BSONDocument(
        "thumbnail" -> BSONString(pic.thumbnail),
        "web" -> BSONString(pic.web)) ++
        (pic.print match {
          case None => BSONDocument()
          case Some(s) => BSONDocument("print" -> BSONString(s))
        }) ++
        (pic.comment match {
          case None => BSONDocument()
          case Some(s) => BSONDocument("comment" -> BSONString(s))
        })
  }


  implicit object GalleryPicsBSONHandler extends BSONDocumentReader[GalleryPics] {

    def read(doc: BSONDocument): GalleryPics = {

      def readPicture(doc: BSONDocument): GalleryPic =
        GalleryPic(
          Const.WebRoot + doc.getAs[BSONString]("thumbnail").get.value,
          doc.getAs[BSONString]("thumbnail").get.value,
          doc.getAs[BSONString]("web").get.value,
          doc.getAs[BSONString]("print") match {
            case None => None
            case Some(s) => Some(s.value)
          },
          doc.getAs[BSONString]("comment") match {
            case None => None
            case Some(s) => Some(s.value)
          })

      def readPictures(option: Option[BSONArray]): List[GalleryPic] = option match {
        case None => List()
        case Some(array) => {
          val stream: Stream[BSONValue] = array.values
          stream.toList.map(value => readPicture(value.asInstanceOf[BSONDocument]))
        }
      }

      GalleryPics(
        doc.getAs[BSONInteger]("categoryId").get.value,
        doc.getAs[BSONInteger]("galleryId").get.value,
        doc.getAs[BSONString]("thumbnail").get.value,
        readPictures(doc.getAs[BSONArray]("pictures")))
    }
  }

}
