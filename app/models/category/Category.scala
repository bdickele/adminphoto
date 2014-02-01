package models.category

import reactivemongo.bson._
import models.util.Access

/**
 * Class standing for a category
 * Created by bdickele
 * Date: 1/11/14
 */
case class Category(id: Option[BSONObjectID],
                    categoryId: Int,
                    rank: Int,
                    title: String,
                    description: Option[String],
                    online: Boolean = true,
                    access: Access.Value = Access.Guest)

object Category {

  implicit object CategoryBSONHandler extends BSONDocumentReader[Category] with BSONDocumentWriter[Category] {

    def read(doc: BSONDocument): Category =
      Category(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[BSONInteger]("categoryId").get.value,
        doc.getAs[BSONInteger]("rank").get.value,
        doc.getAs[BSONString]("title").get.value,
        doc.getAs[BSONString]("description") match {
          case None => None
          case Some(bsonString) => Some(bsonString.value)
        },
        doc.getAs[BSONBoolean]("online").get.value,
        doc.getAs[BSONString]("access").map(s => Access.fromString(s.value)).get)

    def write(c: Category): BSONDocument = {
      var doc = BSONDocument(
        "_id" -> c.id.getOrElse(BSONObjectID.generate),
        "categoryId" -> BSONInteger(c.categoryId),
        "rank" -> BSONInteger(c.rank),
        "title" -> BSONString(c.title),
        "online" -> BSONBoolean(c.online),
        "access" -> BSONString(c.access.asInstanceOf[Access.AccessVal].dbId))

      c.description match {
        case None => // Nothing to do
        case Some(s) => doc = doc ++ BSONDocument("description" -> BSONString(s))
      }

      doc
    }
  }

}
