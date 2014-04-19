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
                    online: Boolean = true)

object Category {

  // Mapper JSON -> Category
  implicit val categoryReader = Json.reads[Category]

  // Mapper Category -> JSON
  implicit val categoryWriter = Json.writes[Category]
}
