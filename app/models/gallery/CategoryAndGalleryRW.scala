package models.gallery

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.Future
import reactivemongo.core.commands.{Unwind, Match, Aggregate}
import reactivemongo.bson.BSONDocument
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Created by bdickele on 16/01/14.
 */
object CategoryAndGalleryRW extends Controller with MongoController {

  def collection = db.collection[BSONCollection]("category")


  def find(categoryId: Int): Future[Option[CategoryAndGallery]] =
    collection.
      find(BSONDocument("categoryId" -> categoryId)).
      one[CategoryAndGallery]


  /**
   * @return All categories, from the most recent to the older
   */
  def findOLD(categoryId: Int): Future[List[GalleryBasic]] = {
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
