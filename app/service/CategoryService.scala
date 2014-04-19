package service

import play.api.mvc.Controller
import play.api.libs.json._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits._
import models.Category
import service.mapper.CategoryMapper._

/**
 * Services related to CRUD operations of categories
 * bdickele
 */
object CategoryService extends Controller with MongoController {

  def collection = db.collection[JSONCollection]("category")


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
      find(Json.obj()).
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

    collection.insert(category)
  }

  // That method updates a category by passing the whole object (an implicit mapper will be used)
  def update(category: Category): Future[LastError] =
    collection.update(
      Json.obj("categoryId" -> category.categoryId),
      category)

  // That method updates a singled field of a document
  def updateField(categoryId: Int, field: String, value: JsValue): Future[LastError] =
    collection.update(
      Json.obj("categoryId" -> categoryId),
      Json.obj("$set" -> Json.obj(field -> value)))
}