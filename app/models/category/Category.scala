package models.category

import reactivemongo.bson._

/**
 * Class standing for a category
 * Created by bdickele
 * Date: 1/11/14
 */
case class Category(categoryId: Int,
                    rank: Int,
                    title: String,
                    description: Option[String],
                    online: Boolean = true)

//access: Access.Value = Access.Guest)
//doc.getAs[BSONString]("access").map(s => Access.fromString(s.value)).get)
//"access" -> BSONString(c.access.asInstanceOf[Access.AccessVal].dbId))

object Category {

  implicit object CategoryBSONHandler extends BSONDocumentReader[Category] with BSONDocumentWriter[Category] {

    def read(doc: BSONDocument): Category =
      Category(
        doc.getAs[BSONInteger]("categoryId").get.value,
        doc.getAs[BSONInteger]("rank").get.value,
        doc.getAs[BSONString]("title").get.value,
        doc.getAs[BSONString]("description") match {
          case None => None
          case Some(bsonString) => Some(bsonString.value)
        },
        doc.getAs[BSONBoolean]("online").get.value)

    def write(c: Category): BSONDocument =
      BSONDocument(
        "categoryId" -> BSONInteger(c.categoryId),
        "rank" -> BSONInteger(c.rank),
        "title" -> BSONString(c.title),
        "online" -> BSONBoolean(c.online)) ++
        (c.description match {
          case None => BSONDocument()
          case Some(s) => BSONDocument("description" -> BSONString(s))
        })
  }

}
