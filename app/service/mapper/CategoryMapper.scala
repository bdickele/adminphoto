package service.mapper

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import models.Category

/**
 * Mapper for class Category
 * bdickele
 */
object CategoryMapper {

  implicit val categoryReader: Reads[Category] = (
    (__ \ "categoryId").read[Int] and
      (__ \ "rank").read[Int] and
      (__ \ "title").read[String] and
      (__ \ "comment").readNullable[String] and
      (__ \ "online").read[Boolean])(Category.apply _)

  implicit val categoryWriter: Writes[Category] = (
    (__ \ "categoryId").write[Int] and
      (__ \ "rank").write[Int] and
      (__ \ "title").write[String] and
      (__ \ "comment").writeNullable[String] and
      (__ \ "online").write[Boolean])(unlift(Category.unapply))
}
