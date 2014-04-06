package models

import util.FtpUtil

/**
 * Case class from folder + facade to collect pictures from the stock
 * bdickele
 */

/**
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
   * @param picturesFolder For instance "2004/0406/"
   * @return List of available picture for in passed folder
   */
  def picturesFromFolder(picturesFolder: String): List[Picture] = {

    val tuple = FtpUtil.loadJpegs(picturesFolder)
    val picturesSmall: List[String] = tuple._1
    val picturesWeb: List[String] = tuple._2
    val picturesPrint: List[String] = tuple._3

    picturesWeb.map(w => Picture(
      picturesFolder,
      picturesSmall.find(p => p.indexOf(w) > -1).getOrElse(""),
      w,
      picturesPrint.find(_.indexOf(w) > -1))).
      toList
  }
}
