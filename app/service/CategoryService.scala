package service

import play.api.mvc.Controller
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import language.postfixOps
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.core.commands._
import models.Category
import reactivemongo.bson.BSONDocument
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.Some
import reactivemongo.core.commands.SumValue
import reactivemongo.core.commands.GroupField
import play.api.libs.json.JsObject
import java.util.concurrent.TimeUnit

/**
 * Services related to CRUD operations of categories
 * bdickele
 */
object CategoryService extends Controller with MongoController {

  def collection = db.collection[JSONCollection]("category")


  // --------------------------------------------------------------
  // Find
  // --------------------------------------------------------------

  def find(categoryId: Int): Future[Option[Category]] =
    collection.
      find(Json.obj("categoryId" -> categoryId)).
      one[Category]

  /** @return complete list of categories from DB, sorted from the
    *         category with greater rank to lower rank */
  def findAll: Future[List[Category]] =
    collection.
      find(Json.obj()).
      sort(Json.obj("rank" -> -1)).
      cursor[Category].
      collect[List]()

  def findAllJson: Future[List[JsObject]] =
    collection.
      find(Json.obj(), Json.obj("_id" -> 0, "categoryId" -> 1, "title" -> 1)).
      sort(Json.obj("rank" -> -1)).
      cursor[JsObject].
      collect[List]()

  /**
   * @return Map : <ul>
   *           <li>Key = category ID</li>
   *           <li>Value = number of galleries</li>
   *           </ul>
   */
  def findNumberOfGalleryByCategory(): Map[Int, Int] = {
    val command = Aggregate("gallery", Seq(
      GroupField("categoryId")("number" -> SumValue(1))
    ))

    val futureBsonDoc = db.command(command)
    val futureJsObject = futureBsonDoc.map(doc => doc.toList.map(Json.toJson(_).as[JsObject]))

    val list: List[JsObject] = Await.result(futureJsObject, Duration(5, TimeUnit.SECONDS))
    list.map(jsObj => ((jsObj \ "_id").as[Int] -> (jsObj \ "number").as[Int])).toMap
  }

  // --------------------------------------------------------------
  // Create & update
  // --------------------------------------------------------------

  def create(title: String, comment: Option[String], online: Boolean): Future[LastError] = {
    val categories = Await.result(findAll, 5 seconds)

    val maxCategoryId = categories.maxBy(_.categoryId).categoryId
    val maxRank = categories.maxBy(_.rank).rank

    val category = Category(
      maxCategoryId + 1,
      maxRank + 1,
      title,
      comment,
      online)

    collection.insert(category)
  }

  // That method updates properties not related to pictures
  def update(categoryId: Int, newTitle: String, newComment: Option[String],
             newOnline: Boolean): Future[LastError] = {
    // Document currently in database
    val future = find(categoryId)
    val currentCategory = Await.result(future, 5 seconds).get

    // Let's update it
    val newCategory = currentCategory.copy(
      title = newTitle,
      comment = newComment,
      online = newOnline)
    collection.update(Json.obj("categoryId" -> categoryId), newCategory)
  }

  // Update using JsObject : more complicated isn't it ?
  def updateWithJsObject(categoryId: Int, newTitle: String, newComment: Option[String], newOnline: Boolean): Future[LastError] = {
    // Document currently in database
    val future = collection.find(Json.obj("categoryId" -> categoryId)).one[JsObject]
    val currentDoc = Await.result(future, 5 seconds).get

    // Let's update it
    val newValues = Json.obj("title" -> newTitle, "online" -> newOnline) ++
      (if (newComment.isDefined) Json.obj("comment" -> newComment.get) else Json.obj())
    val newDoc = currentDoc - "comment" ++ newValues

    collection.update(
      Json.obj("categoryId" -> categoryId),
      newDoc)
  }

  // That method updates a singled field of a document
  def updateField(categoryId: Int, field: String, value: JsValue): Future[LastError] =
    collection.update(
      Json.obj("categoryId" -> categoryId),
      Json.obj("$set" -> Json.obj(field -> value)))

  def delete(categoryId: Int): Future[LastError] = {
    val command = Count("gallery", Some(BSONDocument("categoryId" -> categoryId)))
    val result = db.command(command) // returns Future[Int]

    val nbGalleries = Await.result(result, 5 seconds)
    if (nbGalleries == 0) collection.remove(Json.obj("categoryId" -> categoryId))
    else Future.successful(new LastError(
        ok = false,
        err = Some("Non-empty category"),
        code = None,
        errMsg = Some("You can't delete a non-empty category"),
        None, -1, false))
  }
}