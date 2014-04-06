package service

import play.api.libs.concurrent.Execution.Implicits._
import securesocial.core._
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import scala.concurrent.Await
import scala.concurrent.duration._
import play.api.Logger
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import securesocial.core.providers.Token
import org.joda.time.DateTime
import models.BackEndUser

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
    val user = BackEndUser(
      findMaxUserId + 1,
      identity.identityId,
      identity.firstName,
      identity.lastName,
      identity.fullName,
      identity.email,
      "READER",
      identity.avatarUrl,
      identity.authMethod,
      identity.oAuth1Info,
      identity.oAuth2Info,
      identity.passwordInfo)

    Await.result(collection.insert(Json.toJson(user)), 5 seconds)
    user
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

    future.map {
      list =>
        val filtered: List[Token] = list.filter(!_.isExpired)
        deleteTokens()
        filtered.foreach(save)
    }
  }

  // --------------------------------------------------------------
  // Mappers (reading)
  // --------------------------------------------------------------

  implicit val identityIdReader: Reads[IdentityId] = (
    (__ \ "userId").read[String] and
      (__ \ "providerId").read[String]
    )(IdentityId.apply _)

  implicit object authenticationMethodReader extends Reads[AuthenticationMethod] {
    def reads(json: JsValue): JsResult[AuthenticationMethod] =
      new JsSuccess[AuthenticationMethod](AuthenticationMethod(json.as[String]))
  }

  implicit val oAuth1InfoReader: Reads[OAuth1Info] = (
    (__ \ "token").read[String] and
      (__ \ "secret").read[String]
    )(OAuth1Info.apply _)

  implicit val oAuth2InfoReader: Reads[OAuth2Info] = (
    (__ \ "accessToken").read[String] and
      (__ \ "tokenType").readNullable[String] and
      (__ \ "expiresIn").readNullable[Int] and
      (__ \ "refreshToken").readNullable[String]
    )(OAuth2Info.apply _)

  implicit val passwordInfoReader: Reads[PasswordInfo] = (
    (__ \ "hasher").read[String] and
      (__ \ "password").read[String] and
      (__ \ "salt").readNullable[String]
    )(PasswordInfo.apply _)

  implicit val backEndReader: Reads[BackEndUser] = (
    (__ \ "id").read[Int] and
      (__ \ "identityId").read[IdentityId] and
      (__ \ "firstName").read[String] and
      (__ \ "lastName").read[String] and
      (__ \ "fullName").read[String] and
      (__ \ "email").readNullable[String] and
      (__ \ "role").read[String] and
      (__ \ "avatarUrl").readNullable[String] and
      (__ \ "authMethod").read[AuthenticationMethod] and
      (__ \ "oAuth1Info").readNullable[OAuth1Info] and
      (__ \ "oAuth2Info").readNullable[OAuth2Info] and
      (__ \ "passwordInfo").readNullable[PasswordInfo]
    )(BackEndUser.apply _)

  implicit val tokenReader: Reads[Token] = (
    (__ \ "uuid").read[String] and
      (__ \ "email").read[String] and
      (__ \ "creationTime").read[DateTime] and
      (__ \ "expirationTime").read[DateTime] and
      (__ \ "isSignUp").read[Boolean]
    )(Token.apply _)

  // --------------------------------------------------------------
  // Mappers (writing)
  // --------------------------------------------------------------

  implicit val identityIdWriter: Writes[IdentityId] = (
    (__ \ "userId").write[String] and
      (__ \ "providerId").write[String]
    )(unlift(IdentityId.unapply))

  implicit object authenticationMethodWriter extends Writes[AuthenticationMethod] {
    def writes(authMethod: AuthenticationMethod): JsValue = JsString(authMethod.method)
  }

  implicit val oAuth1InfoWriter: Writes[OAuth1Info] = (
    (__ \ "token").write[String] and
      (__ \ "secret").write[String]
    )(unlift(OAuth1Info.unapply))

  implicit val oAuth2InfoWriter: Writes[OAuth2Info] = (
    (__ \ "accessToken").write[String] and
      (__ \ "tokenType").writeNullable[String] and
      (__ \ "expiresIn").writeNullable[Int] and
      (__ \ "refreshToken").writeNullable[String]
    )(unlift(OAuth2Info.unapply))

  implicit val passwordInfoWriter: Writes[PasswordInfo] = (
    (__ \ "hasher").write[String] and
      (__ \ "password").write[String] and
      (__ \ "salt").writeNullable[String]
    )(unlift(PasswordInfo.unapply))

  implicit val backEndWriter: Writes[BackEndUser] = (
    (__ \ "id").write[Int] and
      (__ \ "identityId").write[IdentityId] and
      (__ \ "firstName").write[String] and
      (__ \ "lastName").write[String] and
      (__ \ "fullName").write[String] and
      (__ \ "email").writeNullable[String] and
      (__ \ "role").write[String] and
      (__ \ "avatarUrl").writeNullable[String] and
      (__ \ "authMethod").write[AuthenticationMethod] and
      (__ \ "oAuth1Info").writeNullable[OAuth1Info] and
      (__ \ "oAuth2Info").writeNullable[OAuth2Info] and
      (__ \ "passwordInfo").writeNullable[PasswordInfo]
    )(unlift(BackEndUser.unapply))

  implicit val tokenWriter: Writes[Token] = (
    (__ \ "uuid").write[String] and
      (__ \ "email").write[String] and
      (__ \ "creationTime").write[DateTime] and
      (__ \ "expirationTime").write[DateTime] and
      (__ \ "isSignUp").write[Boolean]
    )(unlift(Token.unapply))
}