package models.category

import play.api.mvc.Controller
import scala.concurrent.{Await, Future}
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSON, BSONDocument}
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

  def create(title: String, description: String, online: Boolean): Future[LastError] = {
    val categories = Await.result(findAll, Duration(10, TimeUnit.SECONDS))

    val maxCategoryId = categories.maxBy(_.categoryId).categoryId
    val maxRank = categories.maxBy(_.rank).rank

    val category = Category(None,
      maxCategoryId + 1,
      maxRank + 1,
      title,
      if (description == "") None else Some(description),
      online)

    collection.insert(BSON.writeDocument(category))
  }

  def update(category: Category): Future[LastError] =
    collection.update(BSONDocument("_id" -> category.id), BSON.writeDocument(category))
}