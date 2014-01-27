package models.gallery

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.Future
import reactivemongo.bson.BSONDocument
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Created by bdickele on 16/01/14.
 */
object GalleryBasicRW extends Controller with MongoController {

  def collection = db.collection[BSONCollection]("gallery")


  def findAll(categoryId: Int): Future[List[GalleryBasic]] =
    collection.
      find(BSONDocument("categoryId" -> categoryId)).
      sort(BSONDocument("rank" -> -1)).
      cursor[GalleryBasic].
      collect[List]()

  /**
   * As title has to be unique, we need that method
   * @param title
   * @return
   */
  def findByTitle(title: String): Future[Option[GalleryBasic]] =
    collection.
      find(BSONDocument("title" -> title)).
      one[GalleryBasic]


  /*
    val command = Aggregate("category", Seq(
      Match(BSONDocument("categoryId" -> categoryId)),
      Unwind("galleries")))

    val result = db.command(command)
    */
  //result.map(s => )
  //val future: Future[List[GalleryBasic]] = result.map(r => r.toList.map(doc => GalleryBasic.readGallery(doc)))
  //Await.result(future, Duration(1, TimeUnit.SECONDS))

  //Await.result(result, Duration(1, TimeUnit.SECONDS))
  //val list: List[BSONDocument] = result.value.get.get.toList

  //list.map(doc => GalleryBasic.readGallery(doc))


}
