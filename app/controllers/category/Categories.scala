package controllers.category

import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import models.category.Category
import play.api.libs.concurrent.Execution.Implicits._
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument
import play.api.libs.concurrent.Execution.Implicits._

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Categories extends Controller with MongoController {

  def collection = db.collection[BSONCollection]("category")

  val findAllQuery = BSONDocument()


  def view = Action.async {
    val future: Future[List[Category]] = findAll

    future.map {
      categories => Ok(views.html.category.category(categories))
    }.recover {
      case e =>
        e.printStackTrace()
        BadRequest(e.getMessage())
    }
  }

  /** @return All categories, from the most recent to the older */
  def findAll: Future[List[Category]] =
    collection.
      find(findAllQuery).
      sort(BSONDocument("categoryId" -> -1)).
      cursor[Category].
      collect[List]()

}
