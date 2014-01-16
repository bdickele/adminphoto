package models.category

import play.api.mvc.Controller
import scala.concurrent.{Await, Future}
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument
import play.api.libs.concurrent.Execution.Implicits._
import play.api.cache.Cache
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import play.api.Play.current

/**
 * Created by bdickele on 16/01/14
 */
object CategoryRW extends Controller with MongoController {

  //TODO Gestion du cache

  def collection = db.collection[BSONCollection]("category")

  val findAllQuery = BSONDocument()

  val CacheCategory = "CacheCategory"


  /** @return All categories, from the most recent to the older */
  /*
  def loadAll: Future[List[Category]] =
    collection.
      find(findAllQuery).
      sort(BSONDocument("categoryId" -> -1)).
      cursor[Category].
      collect[List]()
    */

  def loadAll: List[Category] =
    Cache.getOrElse[List[Category]](CacheCategory) {
      findCategories
    }

  /** @return complete list of main folders */
  def findCategories: List[Category] = {
    val future: Future[List[Category]] = collection.
      find(findAllQuery).
      sort(BSONDocument("categoryId" -> -1)).
      cursor[Category].
      collect[List]()

    // Ca c'est pas top mais je veux mettre cette liste en cache
    Await.result(future, Duration(5, TimeUnit.SECONDS))
  }

  def clearCache() =
    Cache.getAs[List[Category]](CacheCategory) match {
      case Some(list) => Cache.remove(CacheCategory)
      case None => // Nothing to do
    }
}