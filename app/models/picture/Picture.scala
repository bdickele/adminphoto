package models.picture

import play.api.Play.current
import play.api.cache.Cache
import java.io.File
import models.util.Const

object Picture {

  val FolderWeb = "web/"
  val FolderThumbnail = "thumbnail/"
  val FolderPrint = "print/"

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

  //TODO Mettre les images en cache avec possibilite de vider le cache

  /**
   * @param mainFolder Main folder
   * @param subFolder Section folder
   * @return Complete list of picture of a gallery
   */
  def pictures(mainFolder: String, subFolder: String): List[PicturePath] = {
    val galleryLocal = Const.LocalRoot + mainFolder + "/" + subFolder + "/"
    val galleryUrl = Const.WebRoot + mainFolder + "/" + subFolder + "/"

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
