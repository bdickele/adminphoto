package service

import play.api.Play.current
import play.api.cache.Cache
import util.{Const, FtpUtil}
import java.io.File
import play.api.Play

/**
 * Service used to return list of available folders and sub-folders in the picture stock
 * bdickele
 */
object PictureStockService {

  // That val is used when retrieving available pictures from local computer
  lazy val PhotoStockRootComputer = Play.current.configuration.getString("photostock.root.computer").get

  val CacheMainFolders = "CacheMainFolders"
  val CacheSubFolders = "CacheSubFolders."


  /** Clear cache from main and sub folders */
  def clearCache() =
    Cache.getAs[List[String]](CacheMainFolders) match {
      case Some(list) =>
        // Clearing cache of sub folders
        for (name <- list) {
          Cache.remove(CacheSubFolders + name)
        }
        // Clearing cache of main folders
        Cache.remove(CacheMainFolders)

      case None => // Nothing to do
    }

  /** @return picture folders from cache or load it if not already cached */
  def loadMainFolders(): List[String] =
    Cache.getOrElse[List[String]](CacheMainFolders) {
      if (Const.OffLine) localMainFolders else remoteMainFolders
    }

  /**
   * Loads sub-folders of a main folder (from cache if already cached)
   * @param mainFolderName Name of main folder
   * @return list of sub-folder names
   */
  def loadSubFolders(mainFolderName: String): List[String] =
    Cache.getOrElse[List[String]](CacheSubFolders + mainFolderName) {
      if (Const.OffLine) localSubFolders(mainFolderName) else remoteSubFolders(mainFolderName)
    }

  // --------------------------------------------------------------
  // Getting available pictures stock from pictures hoster
  // --------------------------------------------------------------

  /** @return complete list of main folders */
  def remoteMainFolders: List[String] = FtpUtil.getFolders(None).reverse

  /**
   * @param mainFolderName Name of main folder
   * @return List of sub-folders
   */
  def remoteSubFolders(mainFolderName: String): List[String] = FtpUtil.getFolders(Some(mainFolderName)).reverse

  // --------------------------------------------------------------
  // Getting available pictures stock from local computer
  // in case we can't connect to pictures hoster
  // --------------------------------------------------------------

  def localMainFolders: List[String] = localSubFolders("").reverse

  def localSubFolders(folder: String): List[String] =
    new File(PhotoStockRootComputer + folder).
      listFiles().
      filter(_.isDirectory).
      map(_.getName).
      toList.
      reverse
}
