package models.gallery

import org.joda.time.YearMonth

/**
 * Created by bdickele
 * Date: 1/26/14
 */

case class GalleryForm(categoryId: Int,
                       galleryId: Int,
                       title: String,
                       year: Int,
                       month: Int,
                       description: String,
                       online: Boolean)

object GalleryForm {

  def newOne(categoryId: Int): GalleryForm = {
    val today = new YearMonth()
    GalleryForm(categoryId, -1, "", today.getYear, today.getMonthOfYear, "", true)
  }

  def apply(gallery: Gallery): GalleryForm =
    GalleryForm(gallery.categoryId,
      gallery.galleryId,
      gallery.title,
      gallery.date.getYear,
      gallery.date.getMonthOfYear,
      gallery.description match {
        case None => ""
        case Some(s) => s
      },
      gallery.online)
}
