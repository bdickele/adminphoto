package models

/**
 * Class standing for a category
 * bdickele
 */
case class Category(categoryId: Int,
                    rank: Int,
                    title: String,
                    comment: Option[String],
                    online: Boolean = true)
