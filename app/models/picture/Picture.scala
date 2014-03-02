package models.picture

import java.io.File
import util.Const._

/**
 * Created by bdickele
 * Date: 2/2/14
 */

/**
 *
 * @param folder for instance "2004/0406/"
 * @param thumbnail Name of picture, not its path
 * @param web Same
 * @param print Same
 */
case class Picture(folder: String,
                   thumbnail: String,
                   web: String,
                   print: Option[String])


object Picture {

  /**
   *
   * @param picturesFolder For instance "2004/0406/"
   * @return
   */
  def picturesFromFolder(picturesFolder: String): List[Picture] = {
    val mainFolder = LocalRoot + picturesFolder
    val pathSmall = mainFolder + FolderThumbnail
    val pathWeb = mainFolder + FolderWeb
    val pathPrint = mainFolder + FolderPrint

    val picturesWeb: List[String] = extractJpegs(pathWeb)
    val picturesSmall: List[String] = extractJpegs(pathSmall)
    val picturesPrint: List[String] = extractJpegs(pathPrint)

    picturesWeb.map(w => Picture(
      picturesFolder,
      picturesSmall.find(_.indexOf(w) > -1).getOrElse(""),
      w,
      picturesPrint.find(_.indexOf(w) > -1))).
      toList
  }

  def extractJpegs(folder: String): List[String] =
    new File(folder).
      listFiles().
      filter(f => f.isFile && f.toString.endsWith(".jpg")).
      map(_.getName).
      toList

}
