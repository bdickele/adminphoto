package models.picture

import play.api.Play.current
import play.api.cache.Cache
import java.io.File
import util.Const

/**
 * Created by bdickele
 * Date: 2/2/14
 */
object Folder {

  val CacheMainFolders = "CacheMainFolders"
  val CacheSubFolders = "CacheSubFolders."

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
    foldersOfFolder(Const.LocalRoot).reverse

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
    foldersOfFolder(Const.LocalRoot + mainFolderName).reverse

  def foldersOfFolder(folder: String): List[String] =
    new File(folder).
      listFiles().
      filter(_.isDirectory).
      map(_.getName).
      toList

}
