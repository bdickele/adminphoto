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

  // TODO Mettre LocalRoot dans application.conf
  val LocalRoot = "/Users/bdickele/Dev/www/photostock/"
  val WebRoot = "http://www.dickele.com/photostock/"
  val FolderWeb = "web/"
  val FolderThumbnail = "thumbnail/"
  val FolderPrint = "print/"

  val CacheMainFolders = "mainFolders"
  val CacheSubFolders = "subFolders."

  /** Clear cache from main and sub folders */
  def clearCache =
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
  def loadMainFolders: List[String] =
    Cache.getOrElse[List[String]](CacheMainFolders) {
      mainFolders
    }

  /** @return complete list of main folders */
  def mainFolders: List[String] =
    new File(LocalRoot).
      listFiles().
      filter(_.isDirectory).
      map(_.getName).
      toList.
      reverse

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
  def subFolders(mainFolderName: String): List[String] = {
    println(LocalRoot + mainFolderName)
    new File(LocalRoot + mainFolderName).
      listFiles().
      filter(_.isDirectory).
      map(_.getName).
      toList.
      reverse
  }

  def pictures(mainFolder: String, subFolder: String): List[PicturePath] = {
    val pathThumbnail = LocalRoot + mainFolder + "/" + subFolder + "/" + FolderThumbnail
    val pathThumbnailWeb = WebRoot + mainFolder + "/" + subFolder + "/" + FolderThumbnail

    val picWebs : List[String] = new File(pathThumbnail).
      listFiles().
      filter(f => f.isFile && f.toString.endsWith(".jpg")).
      map(_.getName).
      toList

    picWebs.map(s => PicturePath(pathThumbnailWeb + s, "", s, "", None)).toList
  }

}
