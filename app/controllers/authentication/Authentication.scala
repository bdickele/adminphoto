package controllers.authentication

import play.api.mvc.Request
import play.api.data.Form
import play.api.templates.Html
import securesocial.controllers.DefaultTemplatesPlugin
import securesocial.controllers.Registration.RegistrationInfo
import securesocial.controllers.PasswordChange.ChangeInfo
import securesocial.core.SecuredRequest

/**
 * That plugin is required to display customized views/messages related to authentication
 * bdickele
 */
class Authentication(app: play.api.Application) extends DefaultTemplatesPlugin(app) {

  // Returns the html for the login page
  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)], msg: Option[String] = None): Html = {
    views.html.authentication.login(form, msg)(request.flash)
  }

  // Returns the html for the signup page
  override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = {
    views.html.authentication.signUp(form, token)
  }

  // Returns the html for the start signup page
  override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = {
    views.html.authentication.startSignUp(form)
  }

  // Returns the html for the reset password page
  override def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html = {
    views.html.authentication.startResetPassword(form)
  }

  // Returns the html for the start reset page
  override def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = {
    views.html.authentication.resetPassword(form, token)
  }

  // Returns the html for the change password page (instead of doing a reset)
  override def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]): Html = {
    //TODO views.html.authentication.passwordChange(form)
    super.getPasswordChangePage
  }

  // Returns the html of the Not Authorized page
  override def getNotAuthorizedPage[A](implicit request: Request[A]): Html =
    views.html.global.badRequest("You are not authorized to access that page")
}

