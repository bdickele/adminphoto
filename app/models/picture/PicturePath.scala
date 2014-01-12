package models.picture

/**
 * <p>Paths for a picture : complete and short versions (without root of photo stock).<br>
 * Complete paths are there so that picture can be displayed in the admin web site.<br>
 * Short ones are stored in the DB</p>
 * @param thumbnailComplete Complete path to thumbnail (mandatory)
 * @param webComplete Complete path to web version (mandatory)
 * @param thumbnailShort Short path to thumbnail (mandatory)
 * @param webShort Short path to web version (mandatory)
 * @param printShort Short path to print version (optional)
 */
case class PicturePath(thumbnailComplete: String,
                       webComplete: String,
                       thumbnailShort: String,
                       webShort: String,
                       printShort: Option[String])
