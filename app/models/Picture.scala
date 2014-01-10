package models

import play.api.Play.current
import play.api.cache.Cache
import java.io.File

/**
 * User: bdickele
 * Date: 1/5/14
 */

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

object Picture {

  val LocalRoot = "/Users/bdickele/Dev/www/photostock/"
  val WebRoot = "http://www.dickele.com/photostock/"
  val FolderWeb = "web/"
  val FolderThumbnail = "thumbnail/"
  val FolderPrint = "print/"

  val CacheMainFolders = "mainFolders"
  val CacheSubFolders = "subFolders."

  /** Clear cache from main and sub folders */
  def clearCache() =
    Cache.getAs[List[String]](CacheMainFolders) match {
      case Some(list) => {
        // Clearing cache of sub folders
        for (name <- list) {
          Cache.remove(CacheSubFolders + name)
        }
        // Clearing cache of main folders
        Cache.remove(CacheMainFolders)
      }
      case None => // Nothing to do
    }

  /** @return picture folders from cache or load it if not already cached */
  def loadMainFolders(): List[String] =
    Cache.getOrElse[List[String]](CacheMainFolders) {
      mainFolders
    }

  /** @return complete list of main folders */
  def mainFolders: List[String] =
    foldersOfFolder(LocalRoot).reverse

  /**
   * Loads sub-folders of a main folder (from cache if already cached)
   * @param mainFolderName Name of main folder
   * @return list of sub-folder names
   */
  def loadSubFolders(mainFolderName: String): List[String] =
    Cache.getOrElse[List[String]](CacheSubFolders + mainFolderName) {
      subFolders(mainFolderName)
    }

  /**
   * @param mainFolderName Name of main folder
   * @return List of sub-folders
   */
  def subFolders(mainFolderName: String): List[String] =
    foldersOfFolder(LocalRoot + mainFolderName).reverse

  def foldersOfFolder(folder: String): List[String] =
    new File(folder).
      listFiles().
      filter(_.isDirectory).
      map(_.getName).
      toList

  /**
   * @param mainFolder Main folder
   * @param subFolder Section folder
   * @return Complete list of picture of a gallery
   */
  def pictures(mainFolder: String, subFolder: String): List[PicturePath] = {
    val galleryLocal = LocalRoot + mainFolder + "/" + subFolder + "/"
    val galleryUrl = WebRoot + mainFolder + "/" + subFolder + "/"

    val pathThumbnailLocal = galleryLocal + FolderThumbnail
    val pathThumbnailUrl = galleryUrl + FolderThumbnail
    val pathWebLocal = galleryLocal + FolderWeb
    val pathWebUrl = galleryUrl + FolderWeb
    val pathPrintLocal = galleryLocal + FolderPrint

    val webs: List[String] = picturesFromFolder(pathWebLocal)
    val thumbnails: List[String] = picturesFromFolder(pathThumbnailLocal)
    val prints: List[String] = picturesFromFolder(pathPrintLocal)

    def findThumbnail(webName: String) = thumbnails.find(_.indexOf(webName) > -1).getOrElse("")

    webs.map(w => PicturePath(
      pathThumbnailUrl + findThumbnail(w), pathWebUrl + w,
      findThumbnail(w), w,
      prints.find(_.indexOf(w) > -1))).toList
  }

  /**
   * @param folder Folder name
   * @return Complete list of jpg of passed folder
   */
  def picturesFromFolder(folder: String): List[String] =
    new File(folder).
      listFiles().
      filter(f => f.isFile && f.toString.endsWith(".jpg")).
      map(_.getName).
      toList

}
