package service

import play.api.libs.concurrent.Execution.Implicits._
import securesocial.core._
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import scala.concurrent.Await
import scala.concurrent.duration._
import language.postfixOps
import play.api.Logger
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json._
import play.api.libs.json.Reads._
import securesocial.core.providers.Token
import models.BackEndUser
import models.BackEndUser._
import models.Role

/**
 * Service related to (back-end) users
 * bdickele
 */
class UserService(application: play.api.Application) extends UserServicePlugin(application) with Controller with MongoController {

  val logger = Logger("models.UserService")

  def collection: JSONCollection = db.collection[JSONCollection]("backenduser")

  def tokens: JSONCollection = db.collection[JSONCollection]("backendusertoken")


  def find(id: IdentityId): Option[Identity] = {
    val future = collection.
      find(Json.obj("identityId.userId" -> id.userId, "identityId.providerId" -> id.providerId)).
      one[BackEndUser]

    Await.result(future, 5 seconds)
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    val future = collection.
      find(Json.obj("email" -> email, "identityId.providerId" -> providerId)).
      one[BackEndUser]

    Await.result(future, 5 seconds)
  }

  def findMaxUserId: Int = {
    val future = collection.
      find(Json.obj()).
      sort(Json.obj("id" -> -1)).
      one[JsObject]
    val option: Option[JsObject] = Await.result(future, 5 seconds)

    option match {
      case None => 0
      case Some(doc) => (doc \ "id").as[Int]
    }
  }

  def save(identity: Identity): Identity = {
    find(identity.identityId) match {
      case Some(_) =>
        collection.update(
          Json.obj("identityId" -> identity.identityId),
          Json.obj("$set" -> Json.obj(
            "passwordInfo" -> identity.passwordInfo
          )))
      case None =>
        val user = BackEndUser(
          findMaxUserId + 1,
          createNewAuthId(identity.lastName, identity.firstName),
          identity.identityId,
          identity.firstName,
          identity.lastName,
          identity.fullName,
          identity.email,
          Role.Reader.toString,
          identity.avatarUrl,
          identity.authMethod,
          identity.oAuth1Info,
          identity.oAuth2Info,
          identity.passwordInfo)

        collection.insert(user)
    }
    identity
  }

  def createNewAuthId(lastName: String, firstName: String): String = {
    // List of authIds that start the same
    val newAuthIdPrefix = UserService.buildAuthIdPrefix(lastName, firstName)
    val future = tokens.
      find(Json.obj("authId" -> Json.obj("$regex" -> ("^" + newAuthIdPrefix)))).
      cursor[JsObject].
      collect[List]()
    val list = Await.result(future, 5 seconds)

    val existingAuthIds: List[String] = list.map(obj => (obj \ "authId").as[String])

    UserService.createNewAuthId(newAuthIdPrefix, existingAuthIds)
  }

  def save(token: Token) {
    tokens.insert(Json.toJson(token))
  }

  def findToken(token: String): Option[Token] = {
    val future = tokens.
      find(Json.obj("uuid" -> token)).
      one[Token]
    Await.result(future, 5 seconds)
  }

  def deleteToken(uuid: String) {
    tokens.remove(Json.obj("uuid" -> uuid))
  }

  def deleteTokens() {
    tokens.remove(Json.obj())
  }

  def deleteExpiredTokens() {
    val future = tokens.
      find(Json.obj()).
      cursor[Token].
      collect[List]()

    future.map { list =>
      val filtered: List[Token] = list.filter(!_.isExpired)
      deleteTokens()
      filtered.foreach(save)
    }
  }
}

object UserService {

  def buildAuthIdPrefix(lastName: String, firstName: String) = {
    // In case name's length is < 3
    val maxBound = if (lastName.length < 3) lastName.length else 3
    (lastName.substring(0, maxBound) + firstName.substring(0, 1) + "_").toLowerCase
  }

  def createNewAuthId(prefix: String, existingAuthIds: List[String]): String = {
    val currentMaxCounter: Int = existingAuthIds match {
      case Nil => 0
      case _ => existingAuthIds.map(authId => authId.substring(authId.indexOf("_") + 1).toInt).toList.max
    }
    prefix + (currentMaxCounter + 1)
  }
}