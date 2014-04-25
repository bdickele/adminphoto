package util

import org.apache.commons.net.ftp.{FTPClientConfig, FTPFile, FTPClient}
import play.api.{Logger, Play}

/**
 * Util class to retrieve pictures through FTP
 * bdickele
 */
object FtpUtil {

  lazy val FtpClientAddress = Play.current.configuration.getString("ftp.client.address").get
  lazy val FtpClientLogin = Play.current.configuration.getString("ftp.client.login").get
  lazy val FtpClientPassword = Play.current.configuration.getString("ftp.client.password").get
  lazy val FtpClientPhotoStock = Play.current.configuration.getString("ftp.client.photostock").get

  def load(parentFolder: Option[String])(filterFunction: FTPFile => Boolean): List[String] = {
    val client = new FTPClient()
    val config = new FTPClientConfig((FTPClientConfig.SYST_UNIX))
    try {
      Logger.info("Connecting to pictures hoster through FTP...")
      client.configure(config)
      client.connect(FtpClientAddress)
      client.login(FtpClientLogin, FtpClientPassword)
      Logger.info("Connected")

      val photoStockRoot = FtpClientPhotoStock
      val workingDirectory = parentFolder match {
        case None => photoStockRoot
        case Some(s) => photoStockRoot + s
      }

      val files = client.listFiles(workingDirectory)
      val folders = if (files.isEmpty) List() else files.filter(filterFunction).map(_.getName).toList
      client.logout()
      folders
    } catch {
      case e: Exception => Logger.error(e.getCause + " : " + e.getMessage)
        List()
    } finally {
      try {
        client.disconnect()
        Logger.info("FTP connection closed")
      } catch {
        case e: Exception => Logger.error(e.getMessage)
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

  /**
   * Method called by screen displaying stock of available pictures
   * @param parentFolder Parent folder
   * @return
   */
  def loadJpegs(parentFolder: String): (List[String], List[String], List[String]) = {
    val client = new FTPClient()
    try {
      client.connect(FtpClientAddress)
      client.login(FtpClientLogin, FtpClientPassword)

      val picturesRoot = FtpClientPhotoStock + parentFolder

      def filterFunction (f: FTPFile): Boolean =
        (f.getType == FTPFile.FILE_TYPE) && (f.getName.endsWith(".jpg") || f.getName.endsWith(".jpeg"))

      def getPics(folder: String): List[String] = {
        val folderExist = client.changeWorkingDirectory(picturesRoot + folder)
        if (folderExist) client.listFiles().filter(filterFunction).map(_.getName).toList else List()
      }

      val thumbnails = getPics(Const.FolderThumbnail)
      val webs = getPics(Const.FolderWeb)
      val prints = getPics(Const.FolderPrint)

      client.logout()
      (thumbnails, webs, prints)
    } catch {
      case e: Exception => Logger.error(e.getMessage)
        (Nil, Nil, Nil)
    } finally {
      try {
        client.disconnect()
      } catch {
        case e: Exception => Logger.error(e.getMessage)
      }
    }
  }
}
