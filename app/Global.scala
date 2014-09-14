import com.typesafe.config.ConfigFactory
import java.io.File
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import util.Const

/**
 * Overriding GlobalSettings to add conf files or customize error pages for instance
 * Date: 3/29/14
 */
object Global extends GlobalSettings {

  override def onLoadConfig(config: Configuration, path: File, classloader: ClassLoader, mode: Mode.Mode): Configuration = {
    // We add connection.dev.conf or connection.prod.conf according to running mode
    val modeSpecificConfig = config
      // ++ Configuration(ConfigFactory.load(s"connection.${mode.toString.toLowerCase}.conf"))
    super.onLoadConfig(modeSpecificConfig, path, classloader, mode)
  }

  override def onStart(app: Application) {
    super.onStart(app)

    val onOffLineMessage =
      if (Const.OffLine) "You're offline : ensure you're local tomcat server is running"
      else "You're in online mode"

    prettyLog(
      "You're in " + Play.current.mode.toString.toUpperCase + " mode",
      onOffLineMessage)
  }

  override def onHandlerNotFound(request: RequestHeader) =
    Future.successful(NotFound(views.html.global.notFound(request.path)))

  override def onBadRequest(request: RequestHeader, error: String) =
    Future.successful(BadRequest(views.html.global.badRequest(error)))

  override def onError(request: RequestHeader, ex: Throwable) =
    Future.successful(BadRequest(views.html.global.badRequest(ex.getMessage)))

  def prettyLog(messages: String*) = {
    Logger.info("****************************************************")
    messages.foreach(Logger.info(_))
    Logger.info("****************************************************")
  }
}

