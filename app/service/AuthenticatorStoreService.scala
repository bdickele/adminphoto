package service

import securesocial.core._
import play.api.cache.Cache
import play.api.libs.json._
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

  // Mappers : Json -> Authenticator

  implicit val identityIdReader = Json.reads[IdentityId]

  implicit val authenticatorReader = Json.reads[Authenticator]

  // Mappers : Authenticator -> Json

  implicit val identityWriter = Json.writes[IdentityId]

  implicit val tokenWriter = Json.writes[Authenticator]
}
