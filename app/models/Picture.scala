package models

import play.api.Play.current
import play.api.cache.Cache
import java.io.File

/**
 * User: bdickele
 * Date: 1/5/14
 */

/**
 * Structure of picture folders : basically list of mainf folders with their sub-folders
 * @param mainFolders Main folders
 */
case class PictureFolders(mainFolders: List[FolderYear])

/**
 * "Main" folder
 * @param name Name such as 2004
 * @param folders List of sub-folder names
 */
case class FolderYear(name: String, folders: List[String])

/**
 * <p>Paths for a picture : complete and short versions (without root of photo stock).<br>
 *   Complete paths are there so that picture can be displayed in the admin web site.<br>
 *   Short ones are stored in the DB</p>
 * @param thumbnailComplete Complete path to thumbnail (mandatory)
 * @param webComplete Complete path to web version (mandatory)
 * @param thumbnailShort Short path to thumbnail (mandatory)
 * @param webShort Short path to web version (mandatory)
 * @param printShort Short path to print version (optional)
 */
case class Picture(thumbnailComplete: String,
                   webComplete: String,
                   thumbnailShort: String,
                   webShort: String,
                   printShort: Option[String])

object Picture {

  // TODO Mettre LocalRoot dans application.conf
  val LocalRoot = "/Users/bdickele/Dev/www/photostock/"
  val FolderWeb = "web/"
  val FolderThumbnail = "thumbnail/"
  val FolderPrint = "print/"

  val CachePictureFolders = "pictureFolders"

  /**
   * Clear from cache object PictureFolders and reload it
   * @return Refreshed version of PictureFolders
   */
  def reloadPictureFolders: PictureFolders = {
    Cache.set(CachePictureFolders, None)
    pictureFolders
  }

  /**
   * Get picture folders from cache or load it if not already cached
   * @return PictureFolders
   */
  def pictureFolders: PictureFolders =
    Cache.getAs[PictureFolders](CachePictureFolders) match {
      case Some(pictureFolders) => pictureFolders
      case None => {
        val pictureFolders = PictureFolders(mainFolders)
        Cache.set(CachePictureFolders, pictureFolders)
        pictureFolders
      }
    }

  /**
   * Get complete list of main folders
   * @return list of FolderYear
   */
  def mainFolders: List[FolderYear] =
    new File(LocalRoot).
      listFiles().
      filter(f => f.isDirectory).
      map(f => mainFolder(LocalRoot + f.getName)).
      toList

  /**
   * Get a main folder with the list of its subfolders
   * @param path complete path of main folder
   * @return FolderYear
   */
  def mainFolder(path: String): FolderYear =
    FolderYear(path.substring(path.lastIndexOf("/") + 1),
      new File(path).
        listFiles().
        filter(f => f.isDirectory).
        map(f => f.getName).
        toList)

}
