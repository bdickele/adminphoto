package service

import securesocial.core._
import play.api.cache.Cache
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import securesocial.core.IdentityId
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import scala.concurrent.Await
import scala.concurrent.duration._
import language.postfixOps
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

/**
 * Custom implementation of AuthenticatorStore so that developper is not logged out when server restarts
 * bdickele
 */
class AuthenticatorStoreService(application: play.api.Application) extends AuthenticatorStore(application) with Controller with MongoController {

  def collection: JSONCollection = db.collection[JSONCollection]("authenticator")


  def save(authenticator: Authenticator): Either[Error, Unit] = {
    collection.remove(Json.obj("id" -> authenticator.id))
    collection.insert(Json.toJson(authenticator))
    Cache.set(authenticator.id, authenticator, Authenticator.absoluteTimeoutInSeconds)
    Right(())
  }

  def find(id: String): Either[Error, Option[Authenticator]] = {
    Cache.getAs[Authenticator](id) match {

      case Some(auth) =>
        Right(Option(auth))

      case None =>
        val future = collection.
          find(Json.obj("id" -> id)).
          one[Authenticator]

        Await.result(future, 5 seconds) match {
          case None =>
            Right(None)
          case Some(auth) =>
            Cache.set(auth.id, auth)
            Right(Option(auth))
        }
    }
  }

  def delete(id: String): Either[Error, Unit] = {
    Cache.remove(id)
    collection.remove(Json.obj("id" -> id))
    Right(())
  }

  // --------------------------------------------------------------
  // Mappers (reading)
  // --------------------------------------------------------------

  implicit val identityIdReader: Reads[IdentityId] = (
    (__ \ "userId").read[String] and
      (__ \ "providerId").read[String]
    )(IdentityId.apply _)

  implicit val authenticatorReader: Reads[Authenticator] = (
    (__ \ "id").read[String] and
      (__ \ "identityId").read[IdentityId] and
      (__ \ "creationDate").read[DateTime] and
      (__ \ "lastUsed").read[DateTime] and
      (__ \ "expirationDate").read[DateTime]
    )(Authenticator.apply _)

  // --------------------------------------------------------------
  // Mappers (writing)
  // --------------------------------------------------------------

  implicit val identityIdWriter: Writes[IdentityId] = (
    (__ \ "userId").write[String] and
      (__ \ "providerId").write[String]
    )(unlift(IdentityId.unapply))

  implicit val tokenWriter: Writes[Authenticator] = (
    (__ \ "id").write[String] and
      (__ \ "identityId").write[IdentityId] and
      (__ \ "creationDate").write[DateTime] and
      (__ \ "lastUsed").write[DateTime] and
      (__ \ "expirationDate").write[DateTime]
    )(unlift(Authenticator.unapply))
}
