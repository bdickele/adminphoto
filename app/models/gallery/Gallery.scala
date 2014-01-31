package models.gallery

import org.joda.time.YearMonth
import models.util.{Const, Access}
import reactivemongo.bson._

/**
 * User: bdickele
 * Date: 1/11/14
 */
case class Gallery(id: Option[BSONObjectID],
                   categoryId: Int,
                   galleryId: Int,
                   rank: Int,
                   date: YearMonth,
                   title: String,
                   description: Option[String],
                   thumbnail: String,
                   nbPictures: Int,
                   online: Boolean = true,
                   access: Access.Value = Access.Guest)

object Gallery {

  implicit object GalleryBSONHandler extends BSONDocumentReader[Gallery] with BSONDocumentWriter[Gallery] {

    def read(doc: BSONDocument): Gallery =
      Gallery(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[BSONInteger]("categoryId").get.value,
        doc.getAs[BSONInteger]("galleryId").get.value,
        doc.getAs[BSONInteger]("rank").get.value,
        Gallery.buildYearMonth(doc.getAs[BSONString]("date").get.value),
        doc.getAs[BSONString]("title").get.value,
        doc.getAs[BSONString]("description") match {
          case None => None
          case Some(bsonString) => Some(bsonString.value)
        },
        Const.WebRoot + doc.getAs[BSONString]("thumbnail").get.value,
        doc.getAs[BSONArray]("pictures") match {
          case None => 0
          case Some(array) => array.length
        },
        doc.getAs[BSONBoolean]("online").get.value,
        doc.getAs[BSONString]("access").map(s => Access.fromString(s.value)).get)

    def write(g: Gallery): BSONDocument = {
      var doc = BSONDocument(
        "_id" -> g.id.getOrElse(BSONObjectID.generate),
        "categoryId" -> BSONInteger(g.categoryId),
        "rank" -> BSONInteger(g.rank),
        "galleryId" -> BSONInteger(g.galleryId),
        "date" -> BSONString(g.date.getYear + "/" + g.date.getMonthOfYear),
        "title" -> BSONString(g.title),
        "thumbnail" -> BSONString(g.thumbnail),
        "online" -> BSONBoolean(g.online),
        "access" -> BSONString(g.access.asInstanceOf[Access.AccessVal].dbId))

      g.description match {
        case None => // Nothing to do
        case Some(s) => doc = doc ++ BSONDocument("description" -> BSONString(s))
      }

      doc
    }
  }

  def buildYearMonth(s: String): YearMonth = {
    val data = s.split("/")
    new YearMonth(data(0).toInt, data(1).toInt)
  }
}