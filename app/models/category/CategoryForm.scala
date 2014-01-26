package models.category


/**
 * Class use in the form related to creation or edition of a category
 * Created by bdickele
 * Date: 25/01/14
 */

case class CategoryForm(categoryId: Int,
                        title: String,
                        description: String,
                        online: Boolean)

object CategoryForm {

  val newOne = CategoryForm(-1, "", "", true)

  def apply(category: Category): CategoryForm =
    CategoryForm(category.categoryId,
      category.title,
      category.description match {
        case None => ""
        case Some(s) => s
      },
      category.online)
}
