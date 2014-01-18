package models.category

import play.api.mvc.Controller
import scala.concurrent.Future
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Created by bdickele on 16/01/14
 */
object CategoryRW extends Controller with MongoController {

  def collection = db.collection[BSONCollection]("category")

  val findAllQuery = BSONDocument()


  /** @return complete list of categories from DB, sorted from the
    *         category with greater rank to lower rank */
  def findAll: Future[List[Category]] =
    collection.
      find(findAllQuery).
      sort(BSONDocument("rank" -> -1)).
      cursor[Category].
      collect[List]()
}