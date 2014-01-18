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

  def collection = db.collection[BSONCollection]("category")

  val findAllQuery = BSONDocument()

  val CacheCategory = "CacheCategory"


  /** @return Complete list of categories, from the cache or from DB */
  def findAll: List[Category] =
    Cache.getOrElse[List[Category]](CacheCategory) {
      findCategories
    }

  /** @return complete list of categories from DB */
  def findCategories: List[Category] = {
    val future: Future[List[Category]] = collection.
      find(findAllQuery).
      sort(BSONDocument("rank" -> -1)).
      cursor[Category].
      collect[List]()

    // Is it a good practice ? I don't know to this day, but I need it now
    // so that I can put that list in the cache
    Await.result(future, Duration(5, TimeUnit.SECONDS))
  }

  /** Clear cache from categories */
  def clearCache() =
    Cache.getAs[List[Category]](CacheCategory) match {
      case Some(list) => Cache.remove(CacheCategory)
      case None => // Nothing to do
    }
}