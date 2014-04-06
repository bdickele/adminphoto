package service

import play.api.mvc.Controller
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits._
import models.Category

/**
 * Services related to CRUD operations of categories
 * bdickele
 */
object CategoryService extends Controller with MongoController {

  def collection = db.collection[JSONCollection]("category")

  val findAllQuery = Json.obj()


  // --------------------------------------------------------------
  // Find
  // --------------------------------------------------------------

  def find(categoryId: Int): Future[Option[Category]] =
    collection.
      find(Json.obj("categoryId" -> categoryId)).
      one[Category]

  /** @return complete list of categories from DB, sorted from the
    *         category with greater rank to lower rank */
  def findAll: Future[List[Category]] =
    collection.
      find(findAllQuery).
      sort(Json.obj("rank" -> -1)).
      cursor[Category].
      collect[List]()

  // --------------------------------------------------------------
  // Create & update
  // --------------------------------------------------------------

  def create(title: String, comment: Option[String], online: Boolean): Future[LastError] = {
    val categories = Await.result(findAll, 5 seconds)

    val maxCategoryId = categories.maxBy(_.categoryId).categoryId
    val maxRank = categories.maxBy(_.rank).rank

    val category = Category(
      maxCategoryId + 1,
      maxRank + 1,
      title,
      comment,
      online)

    collection.insert(Json.toJson(category))
  }

  def update(category: Category): Future[LastError] =
    collection.update(
      Json.obj("categoryId" -> category.categoryId),
      Json.toJson(category))

  def updateField(categoryId: Int, field: String, value: JsValue): Future[LastError] =
    collection.update(
      Json.obj("categoryId" -> categoryId),
      Json.obj("$set" -> Json.obj(field -> value)))

  // --------------------------------------------------------------
  // Mappers
  // --------------------------------------------------------------

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