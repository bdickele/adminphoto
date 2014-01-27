package controllers.category

import play.api.mvc.{Action, Controller}
import play.api.data.Forms._
import models.category.{Category, CategoryRW, CategoryForm}
import play.api.data.Form

/**
 * Controller dedicated to category's form (creation and modification)
 * Created by bdickele
 * Date: 25/01/14
 */

object CategoriesForm extends Controller {

  // ---------------------------------------------------------------
  // Mapping with all rules to check + Form[Mapping[CategoryForm]]
  // ---------------------------------------------------------------
  val categoryFormMapping = mapping(
    "categoryId" -> number,
    "title" -> nonEmptyText.
      verifying("Title cannot exceed 50 characters", _.length <= 50),
    "description" -> text.
      verifying("Description cannot exceed 100 characters", _.length <= 100),
    "online" -> boolean)(CategoryForm.apply)(CategoryForm.unapply).

    // Title is to be unique
    verifying("Another category with same title exists",
      category => findByTitle(category.title) match {
        case None => true
        case Some(c) => c.categoryId == category.categoryId
      })

  val categoryForm: Form[CategoryForm] = Form(categoryFormMapping)


  def findByTitle(title: String): Option[Category] =
    Categories.findAllFromCacheOrDB().find(_.title == title)

  def create() = Action {
    Ok(views.html.category.categoryForm("Add a category",
      categoryForm.fill(CategoryForm.newOne)))
  }

  def edit(categoryId: Int) = Action {
    Categories.findAllFromCacheOrDB().find(_.categoryId == categoryId) match {
      case Some(category) => Ok(views.html.category.categoryForm("Category edition",
        categoryForm.fill(CategoryForm(category))))
      case None => Categories.couldNotFindCategory(categoryId)
    }
  }

  def save() = Action {
    implicit request =>
      categoryForm.bindFromRequest.fold(

        // Validation error
        formWithErrors => Ok(views.html.category.categoryForm("Incorrect data for category", formWithErrors)),

        // Validation OK
        form => {
          val categoryId = form.categoryId

          Categories.findAllFromCacheOrDB().find(_.categoryId == categoryId) match {

            // Edition of an existing category
            case Some(category) => CategoryRW.update(
              category.copy(
                title = form.title,
                description = if (form.description.isEmpty) None else Some(form.description),
                online = form.online))

            // New category
            case None => CategoryRW.create(form.title, form.description, form.online)
          }

          Categories.clearCache()
          Redirect(routes.Categories.view())
        }
      )
  }

}
