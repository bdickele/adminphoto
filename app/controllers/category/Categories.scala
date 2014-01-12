package controllers.category

import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import models.category.Category
import play.api.libs.concurrent.Execution.Implicits._

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Categories extends Controller {

  def main = Action.async {
    //TODO Passer la liste des categories a la page HTML
    val future: Future[List[Category]] = CategoryRW.findAll

    future.map {
      categories => Ok(views.html.category.category(categories))
    }.recover {
      case e =>
        e.printStackTrace()
        BadRequest(e.getMessage())
    }

    //Ok(views.html.category.category())
  }

}
