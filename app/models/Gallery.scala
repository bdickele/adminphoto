package models

import org.joda.time.YearMonth
import util.Const

/**
 * Class standing for a gallery
 * bdickele
 */
case class Gallery(categoryId: Int,
                   galleryId: Int,
                   rank: Int,
                   date: YearMonth,
                   title: String,
                   comment: Option[String],
                   thumbnail: String,
                   pictures: List[GalleryPic],
                   online: Boolean = true) {

  def extendedTitle = Gallery.extendedTitle(title, date)

  val nbPictures = pictures.size

  val thumbnailComplete = Const.WebRoot + thumbnail
}

case class GalleryPic(thumbnail: String,
                      web: String,
                      print: Option[String],
                      comment: Option[String]) {

  val thumbnailComplete = Const.WebRoot + thumbnail
}

// Case class dedicated to screen where we update picture's comment
case class GalleryPicComment(categoryId: Int,
                             galleryId: Int,
                             index: Int,
                             webComplete: String,
                             comment: Option[String])

object Gallery {

  def buildYearMonth(s: String): YearMonth = {
    val data = s.split("/")
    new YearMonth(data(0).toInt, data(1).toInt)
  }

  def extendedTitle(title: String, date: YearMonth) =
    "“" + title + "” " + "[" + date.getMonthOfYear + "/" + date.getYear + "]"
}
