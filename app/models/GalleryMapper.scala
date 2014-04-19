package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/**
 * Mapper for class Gallery
 * bdickele
 */
object GalleryMapper {

  // --------------------------------------------------------------
  // Reading
  // --------------------------------------------------------------

  // Mapper: JSON -> GalleryPic
  implicit val galleryPicReader: Reads[GalleryPic] = (
    (__ \ "thumbnail").read[String] and
      (__ \ "web").read[String] and
      (__ \ "print").readNullable[String] and
      (__ \ "comment").readNullable[String]
    )(GalleryPic.apply _)

  // Mapper: JSON -> Gallery
  implicit val galleryReader: Reads[Gallery] = (
    (__ \ "categoryId").read[Int] and
      (__ \ "galleryId").read[Int] and
      (__ \ "rank").read[Int] and
      (__ \ "title").read[String] and
      (__ \ "comment").readNullable[String] and
      (__ \ "thumbnail").read[String] and
      (__ \ "pictures").read[List[GalleryPic]] and
      (__ \ "online").read[Boolean] and
      (__ \ "versioning").read[Versioning])(Gallery.apply _)

  // --------------------------------------------------------------
  // Writing
  // --------------------------------------------------------------

  // Mapper: GalleryPic -> JSON
  implicit val galleryPicWriter: Writes[GalleryPic] = (
    (__ \ "thumbnail").write[String] and
      (__ \ "web").write[String] and
      (__ \ "print").writeNullable[String] and
      (__ \ "comment").writeNullable[String]
    )(unlift(GalleryPic.unapply))

  // Mapper: Gallery -> JSON
  implicit val galleryWriter: Writes[Gallery] = (
    (__ \ "categoryId").write[Int] and
      (__ \ "galleryId").write[Int] and
      (__ \ "rank").write[Int] and
      (__ \ "title").write[String] and
      (__ \ "comment").writeNullable[String] and
      (__ \ "thumbnail").write[String] and
      (__ \ "pictures").write[List[GalleryPic]] and
      (__ \ "online").write[Boolean] and
      (__ \ "versioning").write[Versioning]
    )(unlift(Gallery.unapply))
}
