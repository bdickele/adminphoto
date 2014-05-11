package models

import play.api.libs.json.{Writes, Json, Reads}

/**
 * Class standing for a category
 * bdickele
 */
case class Category(categoryId: Int,
                    rank: Int,
                    title: String,
                    comment: Option[String],
                    online: Boolean = true) {

  private var nbGall = 0

  def nbGallery = nbGall

  def nbGallery_= (x: Int) {
    nbGall = x
  }
}

object Category {

  // Mapper JSON -> Category
  implicit val categoryReader = Json.reads[Category]

  // Mapper Category -> JSON
  implicit val categoryWriter = Json.writes[Category]
}
