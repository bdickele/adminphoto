package views.html.helper


/**
 * Created by bdickele on 25/01/14.
 */
package object bootstrap {

  implicit val fieldConstructor = new FieldConstructor {
    def apply(elements: FieldElements) = bootstrap.bootstrapFieldConstructor(elements)
  }
}
