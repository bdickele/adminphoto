package controllers.authentication

import securesocial.controllers.TemplatesPlugin
import play.api.mvc.{RequestHeader, Request}
import play.api.data.Form
import play.api.templates.{Txt, Html}
import securesocial.core.{Identity, SecuredRequest}
import securesocial.controllers.PasswordChange.ChangeInfo
import securesocial.controllers.Registration.RegistrationInfo

/**
 * That plugin is required to display customized views related to authentication
 * bdickele
 */
/*
class Authentication(application: play.Application) extends TemplatesPlugin {

  // Returns the html for the login page
  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)],
                               msg: Option[String] = None): Html = {
    //views.html.custom.login(form, msg)
    super.getLoginPage
  }

  // Returns the html for the signup page
  override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = {
    //TODO views.html.custom.Registration.signUp(form, token)
    super.getSignUpPage
  }

  // Returns the html for the start signup page
  override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = {
    //TODO views.html.custom.Registration.startSignUp(form)
    super.getStartSignUpPage
  }

  // Returns the html for the reset password page
  override def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html = {
    //TODO views.html.custom.Registration.startResetPassword(form)
    super.getStartResetPasswordPage
  }

  // Returns the html for the start reset page
  def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = {
    //TODO views.html.custom.Registration.resetPasswordPage(form, token)
    super.getResetPasswordPage
  }

  // Returns the html for the change password page
  def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]): Html = {
    //TODO views.html.custom.passwordChange(form)
    super.getPasswordChangePage
  }

  // Returns the email sent when a user starts the sign up process
  def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    //(None, Some(views.html.custom.mails.signUpEmail(token)))
    //TODO
    super.getSignUpEmail(token)
  }

  // Returns the email sent when the user is already registered
  def getAlreadyRegisteredEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    //TODO (None, Some(views.html.custom.mails.alreadyRegisteredEmail(user)))
    super.getAlreadyRegisteredEmail(user)
  }

  // Returns the welcome email sent when the user finished the sign up process
  def getWelcomeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    //TODO (None, Some(views.html.custom.mails.welcomeEmail(user)))
    super.getWelcomeEmail(user)
  }


  // Returns the email sent when a user tries to reset the password but there is no account for
  // that email address in the system
  def getUnknownEmailNotice()(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    //TODO (None, Some(views.html.custom.mails.unknownEmailNotice(request)))
    super.getUnknownEmailNotice()
  }

  // Returns the email sent to the user to reset the password
  def getSendPasswordResetEmail(user: Identity, token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    //TODO (None, Some(views.html.custom.mails.passwordResetEmail(user, token)))
    super.getSendPasswordResetEmail(user, token)
  }

  // Returns the email sent as a confirmation of a password change
  def getPasswordChangedNoticeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    //TODO (None, Some(views.html.custom.mails.passwordChangedNotice(user)))
    super.getPasswordChangedNoticeEmail(user)
  }

  // Returns the html of the Not Authorized page
  def getNotAuthorizedPage[A](implicit request: Request[A]): Html = {
    //TODO views.html.custom.mails.notAuthorizedPage()
    super.getNotAuthorizedPage
  }
}

*/