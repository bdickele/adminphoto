package models.gallery

import org.joda.time.YearMonth
import reactivemongo.bson._
import util.Const

/**
 * Class standing for a gallery, without anything related to pictures, except number of pictures.
 * Created by bdickele
 * Date: 1/11/14
 */
case class Gallery(categoryId: Int,
                   galleryId: Int,
                   rank: Int,
                   date: YearMonth,
                   title: String,
                   comment: Option[String],
                   thumbnail: String,
                   nbPictures: Int,
                   online: Boolean = true) {

  def extendedTitle = Gallery.extendedTitle(title, date)
}


object Gallery {

  implicit object GalleryBSONHandler extends BSONDocumentReader[Gallery] with BSONDocumentWriter[Gallery] {

    def read(doc: BSONDocument): Gallery =
      Gallery(
        doc.getAs[BSONInteger]("categoryId").get.value,
        doc.getAs[BSONInteger]("galleryId").get.value,
        doc.getAs[BSONInteger]("rank").get.value,
        Gallery.buildYearMonth(doc.getAs[BSONString]("date").get.value),
        doc.getAs[BSONString]("title").get.value,
        doc.getAs[BSONString]("comment") match {
          case None => None
          case Some(bsonString) => Some(bsonString.value)
        },
        Const.WebRoot + doc.getAs[BSONString]("thumbnail").get.value,
        doc.getAs[BSONArray]("pictures") match {
          case None => 0
          case Some(array) => array.length
        },
        doc.getAs[BSONBoolean]("online").get.value)

    def write(g: Gallery): BSONDocument =
      BSONDocument(
        "categoryId" -> BSONInteger(g.categoryId),
        "rank" -> BSONInteger(g.rank),
        "galleryId" -> BSONInteger(g.galleryId),
        "date" -> BSONString(g.date.getYear + "/" + g.date.getMonthOfYear),
        "title" -> BSONString(g.title),
        "thumbnail" -> BSONString(g.thumbnail),
        "online" -> BSONBoolean(g.online)) ++
        (g.comment match {
          case None => BSONDocument()
          case Some(s) => BSONDocument("comment" -> BSONString(s))
        })
  }

  def buildYearMonth(s: String): YearMonth = {
    val data = s.split("/")
    new YearMonth(data(0).toInt, data(1).toInt)
  }

  def extendedTitle(title: String, date: YearMonth) =
    "“" + title + "” " + "[" + date.getMonthOfYear + "/" + date.getYear + "]"
}