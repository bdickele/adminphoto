package models.category

import play.api.mvc.Controller
import scala.concurrent.{Await, Future}
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONObjectID, BSONValue, BSON, BSONDocument}
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands.LastError
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

/**
 * Created by bdickele on 16/01/14
 */
object CategoryRW extends Controller with MongoController {

  def collection = db.collection[BSONCollection]("category")

  val findAllQuery = BSONDocument()


  // --------------------------------------------------------------
  // FIND
  // --------------------------------------------------------------

  def find(categoryId: Int): Future[Option[Category]] =
    collection.
      find(BSONDocument("categoryId" -> categoryId)).
      one[Category]

  /** @return complete list of categories from DB, sorted from the
    *         category with greater rank to lower rank */
  def findAll: Future[List[Category]] =
    collection.
      find(findAllQuery).
      sort(BSONDocument("rank" -> -1)).
      cursor[Category].
      collect[List]()

  // --------------------------------------------------------------
  // CREATE
  // --------------------------------------------------------------

  def create(title: String, description: Option[String], online: Boolean): Future[LastError] = {
    val categories = Await.result(findAll, Duration(10, TimeUnit.SECONDS))

    val maxCategoryId = categories.maxBy(_.categoryId).categoryId
    val maxRank = categories.maxBy(_.rank).rank

    val category = Category(
      maxCategoryId + 1,
      maxRank + 1,
      title,
      description,
      online)

    collection.insert(BSON.writeDocument(category))
  }

  // --------------------------------------------------------------
  // UPDATE
  // --------------------------------------------------------------

  def update(category: Category): Future[LastError] =
    collection.update(BSONDocument("categoryId" -> category.categoryId), BSON.writeDocument(category))

  def updateField(categoryId: Int, field: String, value: BSONValue): Future[LastError] =
    collection.update(BSONDocument("categoryId" -> categoryId),
      BSONDocument("$set" -> BSONDocument(field -> value)))
}