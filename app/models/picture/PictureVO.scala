package models.picture

import util.Const._


/**
 * <p>Paths for a picture : complete and short versions (without root of photo stock).<br>
 * Complete paths are there so that picture can be displayed in the admin web site.<br>
 * Short ones are stored in the DB</p>
 * @param thumbnailComplete Complete path to thumbnail (mandatory)
 * @param webComplete Complete path to web version (mandatory)
 * @param thumbnail Short path to thumbnail (mandatory)
 * @param web Short path to web version (mandatory)
 * @param print Short path to print version (optional)
 */
case class PictureVO(thumbnailComplete: String,
                     webComplete: String,
                     thumbnail: String,
                     web: String,
                     print: Option[String])

object PictureVO {

  /**
   * @param mainFolder Main folder
   * @param subFolder Section folder
   * @return Complete list of picture of a gallery
   */
  def pictures(mainFolder: String, subFolder: String): List[PictureVO] = {
    val picturesRaw: List[Picture] = Picture.picturesFromFolder(mainFolder + "/" + subFolder + "/")

    val pathThumbnailUrl = WebRoot + mainFolder + "/" + subFolder + "/" + FolderThumbnail
    val pathWebUrl = WebRoot + mainFolder + "/" + subFolder + "/" + FolderWeb

    picturesRaw.map(picture =>
      PictureVO(
        pathThumbnailUrl + picture.thumbnail,
        pathWebUrl + picture.web,
        picture.thumbnail,
        picture.web,
        picture.print))
  }


}
