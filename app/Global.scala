import com.typesafe.config.ConfigFactory
import java.io.File
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

/**
 * Overriding GlobalSettings to customize error pages for instance
 * Date: 3/29/14
 */
object Global extends GlobalSettings {

  override def onLoadConfig(config: Configuration, path: File, classloader: ClassLoader, mode: Mode.Mode) : Configuration = {
    prettyLog("You're in " + mode.toString.toUpperCase + " mode")

    val modeSpecificConfig = config ++ Configuration(ConfigFactory.load(s"connection.${mode.toString.toLowerCase}.conf"))
    super.onLoadConfig(modeSpecificConfig, path, classloader, mode)
  }

  override def onStart(app: Application) {
    super.onStart(app)
  }

  override def onHandlerNotFound(request: RequestHeader) =
    Future.successful(NotFound(views.html.notFound(request.path)))

  override def onBadRequest(request: RequestHeader, error: String) =
    Future.successful(BadRequest(views.html.badRequest(error)))

  override def onError(request: RequestHeader, ex: Throwable) =
    Future.successful(BadRequest(views.html.badRequest(ex.getMessage)))

  def prettyLog(messages: String*) = {
    Logger.info("****************************************************")
    messages.foreach(Logger.info(_))
    Logger.info("****************************************************")
  }
}

