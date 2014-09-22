package models

import org.joda.time.DateTime
import play.api.libs.json._
import org.joda.time.format.DateTimeFormat

/**
 * Versioning data
 * bdickele
 */
case class Versioning(version: Int,
                      creationDate: DateTime,
                      creationUser: String,
                      updateDate: DateTime,
                      updateUser: String) {

  def increment(user: String) = copy(version = this.version + 1, updateDate = new DateTime(), updateUser = user)

  lazy val updateDateAsStringShort = updateDate.toString(Versioning.DateFormatter_WithoutMillis)
}


object Versioning {

  val pattern_yyyyMMdd_HHmmssSSS = "yyyy-MM-dd HH:mm:ss.SSS"
  val pattern_yyyyMMdd_HHmmss = "yyyy-MM-dd HH:mm:ss"

  val DateFormatter = DateTimeFormat.forPattern(pattern_yyyyMMdd_HHmmssSSS)
  val DateFormatter_WithoutMillis = DateTimeFormat.forPattern(pattern_yyyyMMdd_HHmmss)


  def newOne(user: String) = {
    val date = new DateTime
    Versioning(1, date, user, date, user)
  }

  // --------------------------------------------------------------
  // Mappers (Reads and Writes)
  // --------------------------------------------------------------

  // Mapper used to extract customized DateTime from database
  implicit object dateReader extends Reads[DateTime] {
    def reads(json: JsValue): JsResult[DateTime] =
      new JsSuccess[DateTime](DateFormatter.parseDateTime(json.as[String]))
  }

  // Mapper JSON -> Versioning
  implicit val versioningReader = Json.reads[Versioning]

  // Mapper used to customize JSON format of DateTime
  implicit object dateWriter extends Writes[DateTime] {
    def writes(date: DateTime): JsValue = JsString(date.toString(DateFormatter))
  }

  // Mapper Versioning -> JSON
  implicit val versioningWriter = Json.writes[Versioning]
}
