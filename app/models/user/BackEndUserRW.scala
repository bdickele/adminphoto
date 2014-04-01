package models.user

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import securesocial.core.{Identity, IdentityId}
import scala.concurrent.{Await, Future}
import reactivemongo.bson.{BSON, BSONInteger, BSONDocument}
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import reactivemongo.core.commands.LastError
import play.api.Logger

/**
 * Read/write data from/into collection "user"
 * Created by bdickele on 31/03/14.
 */
object BackEndUserRW extends Controller with MongoController {

  def collection = db.collection[BSONCollection]("user")

  // --------------------------------------------------------------
  // FIND
  // --------------------------------------------------------------

  def findByIdentityId(identityId: IdentityId): Future[Option[BackEndUser]] =
    collection.
      find(BSONDocument(
      "identityId.userId" -> identityId.userId,
      "identityId.providerId" -> identityId.providerId)).
      one[BackEndUser]

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BackEndUser]] =
    collection.
      find(BSONDocument(
      "email" -> email,
      "identityId.providerId" -> providerId)).
      one[BackEndUser]

  def findMaxUserId: Int = {
    val future: Future[Option[BSONDocument]] =
      collection.find(BSONDocument()).
        sort(BSONDocument("userId" -> -1)).
        one[BSONDocument]
    val option: Option[BSONDocument] = Await.result(future, Duration(5, TimeUnit.SECONDS))

    option match {
      case None => 0
      case Some(doc) => doc.getAs[BSONInteger]("userId").get.value
    }
  }

  // --------------------------------------------------------------
  // CREATE
  // --------------------------------------------------------------

  /** Create a gallery (without thumbnail or picture) */
  def create(role: String, identity: Identity): Future[LastError] = {
    val user = BackEndUser(
      findMaxUserId + 1,
      identity.identityId,
      identity.firstName,
      identity.lastName,
      identity.fullName,
      identity.email,
      role,
      identity.avatarUrl,
      identity.authMethod,
      identity.oAuth1Info,
      identity.oAuth2Info,
      identity.passwordInfo)

    Logger.info("User to be created : " + user)
    collection.insert(BSON.writeDocument(user))
  }
}
