package app

import scala.concurrent.{Await, Future}
import play.api.libs.ws.{WS, Response}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

package object controllers {

  val urlPrefix = "http://localhost:9000"
  val callbackURL = urlPrefix + "/callback"

  def futureURL(url: String): Future[Response] =
    WS.url(urlPrefix + "/"+ url).withQueryString("callbackURL" -> callbackURL).get()

  def responseURL(url: String) =
    Await.result(futureURL(url), Duration(5, TimeUnit.SECONDS))
}
