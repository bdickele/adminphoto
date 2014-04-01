package models.user

import _root_.java.util.concurrent.TimeUnit
import play.api.{Logger, Application}
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.IdentityId
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Management of connected users
 * Created by bdickele
 * Date: 29/03/14
 */

class BackEndUserService(application: Application) extends UserServicePlugin(application) {

  val logger = Logger("models.user.BackEndUserService")

  private var tokens = Map[String, Token]()


  def find(id: IdentityId): Option[Identity] =
    Await.result(BackEndUserRW.findByIdentityId(id), Duration(5, TimeUnit.SECONDS))

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] =
    Await.result(BackEndUserRW.findByEmailAndProvider(email, providerId), Duration(5, TimeUnit.SECONDS))

  def save(user: Identity): Identity = {
    if (find(user.identityId).isEmpty) BackEndUserRW.create("READER", user)
    user
  }

  def save(token: Token) {
    tokens += (token.uuid -> token)
  }

  def findToken(token: String): Option[Token] = {
    tokens.get(token)
  }

  def deleteToken(uuid: String) {
    tokens -= uuid
  }

  def deleteTokens() {
    tokens = Map()
  }

  def deleteExpiredTokens() {
    tokens = tokens.filter(!_._2.isExpired)
  }
}