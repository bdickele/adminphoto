package controllers.gallery

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument
import scala.concurrent.Future
import models.gallery.GalleryBasic


/**
 * User: bdickele
 * Date: 1/11/14
 */

object GalleryRW extends Controller with MongoController {

  def collection = db.collection[BSONCollection]("category")


  /**
   * @return All categories, from the most recent to the older
   */
  def findAllBasic(categoryId: Int): Future[List[GalleryBasic]] =
    collection.
      find(BSONDocument("categoryId" -> categoryId)).
      cursor[GalleryBasic].
      collect[List]()

}