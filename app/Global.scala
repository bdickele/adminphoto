import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

/**
 * Overriding GlobalSettings to customize error pages for instance
 * Date: 3/29/14
 */
object Global extends GlobalSettings {

  override def onStart(app: Application) {
    val databaseMessage =
      app.configuration.getString("mongodb.uri") match {
        case None => "Damn, could not find database configuration"
        case Some(dbUri) =>
          if (dbUri.contains("localhost")) "You're running on a LOCAL database"
          else "Keep in mind you're running on a REMOTE database"
      }
    prettyLog(databaseMessage)
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(
      views.html.notFound(request.path)
    ))
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest(
      views.html.badRequest(error)
    ))
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(BadRequest(
      views.html.badRequest(ex.getMessage)
    ))
  }

  def prettyLog(messages: String*) = {
    Logger.info("****************************************************")
    messages.foreach(Logger.info(_))
    Logger.info("****************************************************")
  }
}

