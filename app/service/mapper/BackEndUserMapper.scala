package service.mapper

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import securesocial.core._
import models.BackEndUser
import securesocial.core.providers.Token
import org.joda.time.DateTime

/**
 * Mapper for BackEndUser
 */
object BackEndUserMapper {

  // --------------------------------------------------------------
  // Reading
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
      (__ \ "authId").read[String] and
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
  // Writing
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
      (__ \ "authId").write[String] and
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
