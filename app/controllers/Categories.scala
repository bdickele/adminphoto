package controllers

import play.api.mvc.{Action, Controller}

/**
 * User: bdickele
 * Date: 1/7/14
 */
object Categories extends Controller {

  def main = Action {
    Ok(views.html.category.category())
  }

}
