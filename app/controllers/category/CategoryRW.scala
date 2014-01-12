package controllers.category

import play.api.mvc.Controller

import play.modules.reactivemongo.MongoController
import reactivemongo.bson._
import models.category.Category
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.api.collections.default.BSONCollection

/**
 * User: bdickele
 * Date: 1/11/14
 */
object CategoryRW extends Controller with MongoController {

  def collection = db.collection[BSONCollection]("category")

  val findAllQuery = BSONDocument()


  /**
   * @return All categories, from the most recent to the older
   */
  def findAll: Future[List[Category]] =
    collection.
      find(findAllQuery).
      sort(BSONDocument("categoryId" -> -1)).
      cursor[Category].
      collect[List]()

}
