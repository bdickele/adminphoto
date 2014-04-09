package models

import util.Const

/**
 * Class standing for a gallery
 * bdickele
 */
case class Gallery(categoryId: Int,
                   galleryId: Int,
                   rank: Int,
                   title: String,
                   comment: Option[String],
                   thumbnail: String,
                   pictures: List[GalleryPic],
                   online: Boolean = true) {

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
