package models

import securesocial.core._
import play.api.mvc.RequestHeader
import play.api.libs.json._
import securesocial.core.OAuth2Info
import play.api.libs.json.JsSuccess
import securesocial.core.OAuth1Info
import securesocial.core.IdentityId
import securesocial.core.PasswordInfo
import securesocial.core.SecuredRequest
import securesocial.core.providers.Token

/**
 * Class for users
 * bdickele
 */
case class BackEndUser(id: Int,
                       authId: String,
                       identityId: securesocial.core.IdentityId,
                       firstName: String,
                       lastName: String,
                       fullName: String,
                       email: Option[String],
                       role: String,
                       avatarUrl: Option[String],
                       authMethod: securesocial.core.AuthenticationMethod,
                       oAuth1Info: Option[securesocial.core.OAuth1Info],
                       oAuth2Info: Option[securesocial.core.OAuth2Info],
                       passwordInfo: Option[securesocial.core.PasswordInfo]) extends Identity {

  val isWriter = BackEndUser.isWriter(role)

  val isReader = BackEndUser.isReader(role)
}


object BackEndUser {

  def user(request: RequestHeader) = request.asInstanceOf[SecuredRequest[Any]].user.asInstanceOf[BackEndUser]

  def isReader(request: SecuredRequest[Any]) = !isWriter(request)

  def isReader(request: RequestHeader): Boolean = !isWriter(request)

  def isWriter(request: SecuredRequest[Any]): Boolean = isWriter(request.user.asInstanceOf[BackEndUser].role)

  def isWriter(request: RequestHeader): Boolean = isWriter(request.asInstanceOf[SecuredRequest[Any]])

  // Central point for business logic related to: is a role of type Writer or Reader
  def isWriter(s: String) = s == Role.Writer.toString

  def isReader(s: String) = !isWriter(s)

  // --------------------------------------------------------------
  // Mappers JSON -> BackEndUser (Reads)
  // --------------------------------------------------------------

  implicit val identityIdReader = Json.reads[IdentityId]

  implicit object authenticationMethodReader extends Reads[AuthenticationMethod] {
    def reads(json: JsValue): JsResult[AuthenticationMethod] =
      new JsSuccess[AuthenticationMethod](AuthenticationMethod(json.as[String]))
  }

  implicit val oAuth1InfoReader = Json.reads[OAuth1Info]

  implicit val oAuth2InfoReader = Json.reads[OAuth2Info]

  implicit val passwordInfoReader = Json.reads[PasswordInfo]

  implicit val backEndReader = Json.reads[BackEndUser]

  implicit val tokenReader = Json.reads[Token]

  // --------------------------------------------------------------
  // Mappers BackEndUser -> JSON (Writes)
  // --------------------------------------------------------------

  implicit val identityIdWriter = Json.writes[IdentityId]

  implicit object authenticationMethodWriter extends Writes[AuthenticationMethod] {
    def writes(authMethod: AuthenticationMethod): JsValue = JsString(authMethod.method)
  }

  implicit val oAuth1InfoWriter = Json.writes[OAuth1Info]

  implicit val oAuth2InfoWriter = Json.writes[OAuth2Info]

  implicit val passwordInfoWriter = Json.writes[PasswordInfo]

  implicit val backEndWriter = Json.writes[BackEndUser]

  implicit val tokenWriter = Json.writes[Token]

}

// Available values for role
object Role extends Enumeration {
  val Reader = Value("READER")
  val Writer = Value("WRITER")
}

// That class is used by securesocial's SecuredAction to check user role
case class WithRole(role: Role.Value) extends Authorization {

  def isAuthorized(user: Identity) = role match {
    case Role.Writer => BackEndUser.isWriter(user.asInstanceOf[BackEndUser].role)
    case _ => true
  }
}