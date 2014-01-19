package models.gallery

import org.joda.time.YearMonth
import models.util.Access
import reactivemongo.bson.BSONObjectID

/**
 * User: bdickele
 * Date: 1/11/14
 */

case class GalleryPicture(thumbnail: String,
                          web: String,
                          print: Option[String],
                          description: Option[String])

case class Gallery(id: Option[BSONObjectID],
                   categoryId: Int,
                   galleryId: String,
                   rank: Int,
                   date: YearMonth,
                   title: String,
                   description: String,
                   thumbnail: String,
                   online: Boolean,
                   access: Access.Value,
                   pictures: List[GalleryPicture])

object Gallery {

  def buildDate(s: String): YearMonth = {
    val data = s.split("/")
    new YearMonth(data(0).toInt, data(1).toInt)
  }
}