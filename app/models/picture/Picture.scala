package models.picture

import java.io.File
import util.Const._
import util.FtpUtil

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

    val tuple = FtpUtil.loadJpegs(picturesFolder)
    val picturesWeb: List[String] = tuple._1
    val picturesSmall: List[String] = tuple._2
    val picturesPrint: List[String] = tuple._3

    def findMatchingThumbnail(webPic: String): String =
      picturesSmall.find(p => p.indexOf(webPic) > -1).getOrElse("")


    picturesWeb.map(w => Picture(
      picturesFolder,
      //picturesSmall.find(_.indexOf(w) > -1).getOrElse(""),
      findMatchingThumbnail(w),
      w,
      picturesPrint.find(_.indexOf(w) > -1))).
      toList
  }
}
