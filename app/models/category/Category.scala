package models.category

import reactivemongo.bson._
import models.util.Access

/**
 * User: bdickele
 * Date: 1/11/14
 */
case class Category(id: Option[BSONObjectID],
                    categoryId: Int,
                    rank: Int,
                    title: String,
                    description: String,
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
        doc.getAs[BSONString]("description").get.value,
        doc.getAs[BSONBoolean]("online").get.value,
        doc.getAs[BSONString]("access").map(s => Access.fromString(s.value)).get)

    def write(c: Category) =
      BSONDocument(
        "_id" -> c.id.getOrElse(BSONObjectID.generate),
        "categoryId" -> BSONInteger(c.categoryId),
        "rank" -> BSONInteger(c.rank),
        "title" -> BSONString(c.title),
        "description" -> BSONString(c.description),
        "online" -> BSONBoolean(c.online),
        "access" -> BSONString(c.access.asInstanceOf[Access.AccessVal].dbId))
  }
}
