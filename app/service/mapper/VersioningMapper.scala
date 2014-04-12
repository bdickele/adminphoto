package service.mapper

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import models.Versioning
import org.joda.time.format.DateTimeFormat

/**
 * Mapper for Versioning
 * bdickele
 */
object VersioningMapper {

  val pattern = "yyyy-MM-dd HH:mm:ss.SSS"

  val DateFormatter = DateTimeFormat.forPattern(pattern)


  // --------------------------------------------------------------
  // Reading
  // --------------------------------------------------------------

  implicit object dateReader extends Reads[DateTime] {
    def reads(json: JsValue): JsResult[DateTime] =
      new JsSuccess[DateTime](DateFormatter.parseDateTime(json.as[String]))
  }

  implicit val versioningReader: Reads[Versioning] = (
    (__ \ "version").read[Int] and
      (__ \ "creationDate").read[DateTime] and
      (__ \ "creationUser").read[String] and
      (__ \ "updateDate").read[DateTime] and
      (__ \ "updateUser").read[String]
    )(Versioning.apply _)

  // --------------------------------------------------------------
  // Writing
  // --------------------------------------------------------------

  implicit object dateWriter extends Writes[DateTime] {
    def writes(date: DateTime): JsValue = JsString(date.toString(DateFormatter))
  }

  implicit val versioningWriter: Writes[Versioning] = (
    (__ \ "version").write[Int] and
      (__ \ "creationDate").write[DateTime] and
      (__ \ "creationUser").write[String] and
      (__ \ "updateDate").write[DateTime] and
      (__ \ "updateUser").write[String]
    )(unlift(Versioning.unapply))
}