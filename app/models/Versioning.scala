package models

import org.joda.time.DateTime

/**
 * Versioning data
 * bdickele
 */
case class Versioning(version: Int,
                      creationDate: DateTime,
                      creationUser: String,
                      updateDate: DateTime,
                      updateUser: String) {

  def increment(user: String) = copy(version = this.version + 1, updateDate = new DateTime(), updateUser = user)
}


object Versioning {

  def newOne(user: String) = {
    val date = new DateTime
    Versioning(1, date, user, date, user)
  }
}
