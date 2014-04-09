package models


/**
 * Object used for gallery's form (without anything related to pictures)
 * bdickele
 */
case class GalleryForm(categoryId: Int,
                       galleryId: Int,
                       title: String,
                       comment: String,
                       online: Boolean = true)

object GalleryForm {

  def newOne(categoryId: Int): GalleryForm = GalleryForm(categoryId, -1, "", "")

  def apply(gallery: Gallery): GalleryForm =
    GalleryForm(gallery.categoryId,
      gallery.galleryId,
      gallery.title,
      gallery.comment match {
        case None => ""
        case Some(s) => s
      },
      gallery.online)
}
