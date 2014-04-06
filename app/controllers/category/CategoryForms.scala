package controllers.category

import play.api.mvc.{Action, Controller}
import play.api.data.Forms._
import play.api.data.Form
import securesocial.core.SecureSocial
import service.CategoryService
import models.Category

/**
 * Controller dedicated to category's form (creation and modification)
 * bdickele
 */
object CategoryForms extends Controller with SecureSocial {

  // ---------------------------------------------------------------
  // Mapping with all rules to check + Form[Mapping[CategoryForm]]
  // ---------------------------------------------------------------
  val categoryMapping = mapping(
    "categoryId" -> number,
    "rank" -> ignored(-1),
    "title" -> nonEmptyText.
      verifying("Title cannot exceed 50 characters", _.length <= 50),
    "comment" -> optional(text).
      verifying("Comment cannot exceed 100 characters", _ match {
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

  def create() = SecuredAction {
    implicit request =>
      Ok(views.html.category.categoryForm("New category",
        categoryForm.fill(Category(-1, -1, "", None))))
  }

  def edit(categoryId: Int) = SecuredAction {
    implicit request =>
      Categories.findAllFromCacheOrDB().find(_.categoryId == categoryId) match {
        case Some(category) =>
          Ok(views.html.category.categoryForm("Category \"" + category.title + "\"", categoryForm.fill(category)))
        case None =>
          Categories.couldNotFindCategory(categoryId)
      }
  }

  def save() = SecuredAction {
    implicit request =>
      categoryForm.bindFromRequest.fold(

        // Validation error
        formWithErrors => Ok(views.html.category.categoryForm("Incorrect data for category", formWithErrors)),

        // Validation OK
        form => {
          Categories.findAllFromCacheOrDB().find(_.categoryId == form.categoryId) match {

            // Edition of an existing category
            case Some(category) => CategoryService.update(
              category.copy(
                title = form.title,
                comment = form.comment,
                online = form.online))

            // New category
            case None => CategoryService.create(form.title, form.comment, form.online)
          }

          Categories.clearCache()
          Redirect(routes.Categories.view())
        }
      )
  }

}
