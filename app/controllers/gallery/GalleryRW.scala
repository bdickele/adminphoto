package controllers.gallery

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import models.gallery.GalleryBasic
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit


/**
 * User: bdickele
 * Date: 1/11/14
 */

object GalleryRW extends Controller with MongoController {

  def collection = db.collection[BSONCollection]("category")


  /**
   * @return All categories, from the most recent to the older
   */
  def findAllBasic(categoryId: Int): Future[List[GalleryBasic]] = {
    /*
      collection.
        find(BSONDocument("categoryId" -> categoryId)).
        cursor[GalleryBasic].
        collect[List]()
    */
    val command = Aggregate("category", Seq(
      Match(BSONDocument("categoryId" -> categoryId)),
      Unwind("galleries")))

    val result = db.command(command)

     //result.map(s => )
    //val future: Future[List[GalleryBasic]] = result.map(r => r.toList.map(doc => GalleryBasic.readGallery(doc)))
    //Await.result(future, Duration(1, TimeUnit.SECONDS))

    //Await.result(result, Duration(1, TimeUnit.SECONDS))
    //val list: List[BSONDocument] = result.value.get.get.toList

    //list.map(doc => GalleryBasic.readGallery(doc))
    //future


    Future[List[GalleryBasic]](List())
  }
}