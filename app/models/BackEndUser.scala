package models

import securesocial.core._
import play.api.mvc.RequestHeader

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