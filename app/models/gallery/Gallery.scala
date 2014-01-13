package models.gallery

import org.joda.time.YearMonth
import reactivemongo.bson.BSONObjectID
import models.util.Access

/**
 * User: bdickele
 * Date: 1/11/14
 */

case class GalleryPicture(thumbnail: String,
                          web: String,
                          print: Option[String])

case class Gallery(sectionId: Int,
                   galleryId: String,
                   date: YearMonth,
                   title: String,
                   description: String,
                   thumbnail: String,
                   online: Boolean,
                   access: Access.Value,
                   pictures: List[GalleryPicture])