package controllers.category

import play.api.mvc.{Action, Controller}
import play.api.data.Forms._
import models.category.{Category, CategoryRW}
import play.api.data.Form

/**
 * Controller dedicated to category's form (creation and modification)
 * Created by bdickele
 * Date: 25/01/14
 */

object CategoryForms extends Controller {

  // ---------------------------------------------------------------
  // Mapping with all rules to check + Form[Mapping[CategoryForm]]
  // ---------------------------------------------------------------
  val categoryMapping = mapping(
    "categoryId" -> number,
    "rank" -> ignored(-1),
    "title" -> nonEmptyText.
      verifying("Title cannot exceed 50 characters", _.length <= 50),
    "description" -> optional(text).
      verifying("Description cannot exceed 100 characters", _ match {
      case None => true
      case Some(d) => d.length <= 100
    }),
    "online" -> boolean)(Category.apply)(Category.unapply).

    // Title is to be unique
    verifying("Another category with same title exists",
      category => findByTitle(category.title) match {
        case None => true
        case Some(c) => c.categoryId == category.categoryId
      })

  val categoryForm: Form[Category] = Form(categoryMapping)


  def findByTitle(title: String): Option[Category] =
    Categories.findAllFromCacheOrDB().find(_.title == title)

  def create() = Action {
    Ok(views.html.category.categoryForm("Add a category",
      categoryForm.fill(Category(-1, -1, "", None, true))))
  }

  def edit(categoryId: Int) = Action {
    Categories.findAllFromCacheOrDB().find(_.categoryId == categoryId) match {
      case Some(category) => Ok(views.html.category.categoryForm("Category edition",
        categoryForm.fill(category)))
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
          println("categoryId = " + categoryId)

          Categories.findAllFromCacheOrDB().find(_.categoryId == categoryId) match {

            // Edition of an existing category
            case Some(category) => CategoryRW.update(
              category.copy(
                title = form.title,
                description = form.description,
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
