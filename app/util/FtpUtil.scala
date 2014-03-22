package util

import org.apache.commons.net.ftp.{FTPFile, FTPClient}
import play.api.Play

/**
 * Created by bdickele
 * Date: 3/16/14
 */
object FtpUtil {

  lazy val FtpClientAddress = Play.current.configuration.getString("ftp.client.address").get
  lazy val FtpClientLogin = Play.current.configuration.getString("ftp.client.login").get
  lazy val FtpClientPassword = Play.current.configuration.getString("ftp.client.password").get
  lazy val FtpClientPhotoStock = Play.current.configuration.getString("ftp.client.photostock").get

  def load(parentFolder: Option[String])(filterFunction: FTPFile => Boolean): List[String] = {
    val client = new FTPClient()
    try {
      client.connect(FtpClientAddress)
      client.login(FtpClientLogin, FtpClientPassword)

      val photoStockRoot = FtpClientPhotoStock
      client.changeWorkingDirectory(parentFolder match {
        case None => photoStockRoot
        case Some(s) => photoStockRoot + "/" + s
      })

      //Logger.info("FTPClient dir : " + client.printWorkingDirectory())

      val folders = client.listFiles().filter(filterFunction)

      client.logout()
      folders.map(f => f.getName).toList
    } catch {
      case e: Exception => e.printStackTrace()
        List()
    } finally {
      try {
        client.disconnect()
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }

  def getFolders(parentFolder: Option[String]): List[String] =
    load(parentFolder) {
      f => (f.getType == FTPFile.DIRECTORY_TYPE) && !f.getName.contains(".")
    }

  def getPictures(parentFolder: String): List[String] =
    load(Some(parentFolder)) {
      f => (f.getType == FTPFile.FILE_TYPE) && (
        f.getName.endsWith(".jpg") || f.getName.endsWith(".jpeg"))
    }

  def loadJpegs(parentFolder: String): (List[String], List[String], List[String]) = {
    val client = new FTPClient()
    try {
      client.connect("ftp.perso.ovh.net")
      client.login("dickele", "Espace34")

      val picturesRoot = "/www/photostock/" + parentFolder

      def filterFunction (f: FTPFile): Boolean = {
        (f.getType == FTPFile.FILE_TYPE) && (f.getName.endsWith(".jpg") || f.getName.endsWith(".jpeg"))
      }

      def getPics(folder: String): List[String] = {
        val folderExist = client.changeWorkingDirectory(picturesRoot + folder)
        //Logger.info("FTPClient dir : " + client.printWorkingDirectory())
        if (folderExist) client.listFiles().filter(filterFunction).map(_.getName).toList else List()
      }

      val thumbnails = getPics(Const.FolderThumbnail)
      val webs = getPics(Const.FolderWeb)
      val prints = getPics(Const.FolderPrint)

      client.logout()
      (thumbnails, webs, prints)
    } catch {
      case e: Exception => e.printStackTrace()
        (Nil, Nil, Nil)
    } finally {
      try {
        client.disconnect()
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }
}
