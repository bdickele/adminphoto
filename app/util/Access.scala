package util

/**
 * User: bdickele
 * Date: 1/11/14
 */
object Access extends Enumeration {

  case class AccessVal(dbId: String) extends Val

  val Guest = AccessVal("G")
  val User = AccessVal("U")

  def fromString(s: String): Access.Value =
    Access.values.find(v => v.asInstanceOf[AccessVal].dbId == s).getOrElse(
      throw new NoSuchElementException("Value " + s + "doesn't stand for an existing access authorization"))

}