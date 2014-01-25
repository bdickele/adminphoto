package controllers.category

import play.api.mvc.{Action, Controller}
import play.api.data.Forms._
import models.category.{CategoryRW, CategoryForm}
import play.api.data.Form

/**
 * Created by bdickele
 * Date: 25/01/14
 */

object CategoriesForm extends Controller {

  val categoryFormMapping = mapping(
    "categoryId" -> number,
    "title" -> nonEmptyText.verifying("Title cannot exceed 50 characters", _.length <= 50),
    "description" -> text.verifying("Description cannot exceed 100 characters", _.length <= 100),
    "online" -> boolean)(CategoryForm.apply)(CategoryForm.unapply)

  val categoryForm: Form[CategoryForm] = Form(categoryFormMapping)


  def edit(categoryId: Int) = Action {
    Categories.findAllFromCacheOrDB().find(_.categoryId == categoryId) match {
      case Some(category) => {
        Ok(views.html.category.categoryForm("Category edition",
          categoryForm.fill(CategoryForm(category))))
      }
      case None => Categories.couldNotFindCategory(categoryId)
    }
  }

  def update() = Action {
    implicit request =>
      categoryForm.bindFromRequest.fold(
        formWithErrors => Ok(views.html.category.categoryForm("Category edition", formWithErrors)),
        categoryForm => {
          val categoryId = categoryForm.categoryId

          Categories.findAllFromCacheOrDB().find(_.categoryId == categoryId) match {
            case Some(category) => {
              Categories.clearCache()
              CategoryRW.update(category.copy(
                title = categoryForm.title,
                description = categoryForm.description,
                online = categoryForm.online))
              Redirect(routes.Categories.view())
            }
            case None => Categories.couldNotFindCategory(categoryId)
          }
        }
      )
  }

}
