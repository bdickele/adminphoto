package models

import securesocial.core._
import play.api.mvc.RequestHeader

/**
 * Class for users
 * bdickele
 */
case class BackEndUser(id: Int,
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

  val isWriter = role == "WRITER"

  val isReader = !isWriter
}


object BackEndUser {

  def user(request: RequestHeader) = request.asInstanceOf[SecuredRequest[Any]].user.asInstanceOf[BackEndUser]

  def isReader(request: SecuredRequest[Any]) = !isWriter(request)

  def isReader(request: RequestHeader) : Boolean = !isWriter(request)

  def isWriter(request: SecuredRequest[Any]) = request.user.asInstanceOf[BackEndUser].isWriter

  def isWriter(request: RequestHeader) : Boolean = isWriter(request.asInstanceOf[SecuredRequest[Any]])

}