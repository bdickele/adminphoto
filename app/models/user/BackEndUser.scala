package models.user

import securesocial.core._
import reactivemongo.bson._
import securesocial.core.IdentityId
import reactivemongo.bson.BSONString
import scala.Some
import reactivemongo.bson.BSONInteger
import securesocial.core.OAuth1Info

/**
 * Created by bdickele
 * Date: 30/03/14
 */
case class BackEndUser(id: Int,
                       identityId: securesocial.core.IdentityId,
                       firstName: String,
                       lastName: String,
                       fullName: String,
                       email: Option[String],
                       role: String,
                       avatarUrl: Option[String],
                       authMethod: securesocial.core.AuthenticationMethod,
                       oAuth1Info: Option[securesocial.core.OAuth1Info],
                       oAuth2Info: Option[securesocial.core.OAuth2Info],
                       passwordInfo: Option[securesocial.core.PasswordInfo]) extends Identity {

  val isReader = role != "WRITER"
  val isWriter = role == "WRITER"
}


object BackEndUser {

  implicit object BackEndUserBSONHandler extends BSONDocumentReader[BackEndUser] with BSONDocumentWriter[BackEndUser] {

    // ------------------------------------------------------
    // READER
    // ------------------------------------------------------

    def buildIdentityId(doc: BSONDocument) =
      IdentityId(doc.getAs[BSONString]("userId").get.value, doc.getAs[BSONString]("providerId").get.value)

    def buildOAuth1Info(doc: BSONDocument) =
      OAuth1Info(doc.getAs[BSONString]("token").get.value, doc.getAs[BSONString]("secret").get.value)

    def buildOAuth2Info(doc: BSONDocument) =
      OAuth2Info(
        doc.getAs[BSONString]("accessToken").get.value,
        doc.getAs[BSONString]("tokenType") match {
          case None => None
          case Some(bsonString) => Some(bsonString.value)
        })

    def buildPasswordInfo(doc: BSONDocument) =
      PasswordInfo(
        doc.getAs[BSONString]("hasher").get.value,
        doc.getAs[BSONString]("password").get.value,
        doc.getAs[BSONString]("salt") match {
          case None => None
          case Some(bsonString) => Some(bsonString.value)
        })

    def read(doc: BSONDocument) =
      BackEndUser(
        doc.getAs[BSONInteger]("id").get.value,
        buildIdentityId(doc.getAs[BSONDocument]("identityId").get),
        doc.getAs[BSONString]("firstName").get.value,
        doc.getAs[BSONString]("lastName").get.value,
        doc.getAs[BSONString]("fullName").get.value,
        doc.getAs[BSONString]("email") match {
          case None => None
          case Some(bsonString) => Some(bsonString.value)
        },
        doc.getAs[BSONString]("role").get.value,
        doc.getAs[BSONString]("avatarUrl") match {
          case None => None
          case Some(bsonString) => Some(bsonString.value)
        },
        AuthenticationMethod(doc.getAs[BSONString]("authMethod").get.value),
        doc.getAs[BSONDocument]("oAuth1Info") match {
          case None => None
          case Some(subDoc) => Some(buildOAuth1Info(subDoc))
        },
        doc.getAs[BSONDocument]("oAuth2Info") match {
          case None => None
          case Some(subDoc) => Some(buildOAuth2Info(subDoc))
        },
        doc.getAs[BSONDocument]("passwordInfo") match {
          case None => None
          case Some(subDoc) => Some(buildPasswordInfo(subDoc))
        }
      )

    // ------------------------------------------------------
    // WRITER
    // ------------------------------------------------------

    def write(u: BackEndUser): BSONDocument =
      BSONDocument(
        "id" -> BSONInteger(u.id),
        "identityId" -> BSONDocument(
          "userId" -> BSONString(u.identityId.userId),
          "providerId" -> BSONString(u.identityId.providerId)),
        "firstName" -> BSONString(u.firstName),
        "lastName" -> BSONString(u.lastName),
        "fullName" -> BSONString(u.fullName),
        "role" -> BSONString(u.role),
        "authMethod" -> BSONString(u.authMethod.method)) ++
        (u.email match {
          case None => BSONDocument()
          case Some(s) => BSONDocument("email" -> BSONString(s))
        }) ++
        (u.oAuth1Info match {
          case None => BSONDocument()
          case Some(o) => BSONDocument("oAuth1Info" ->
            BSONDocument("token" -> o.token, "secret" -> o.secret))
        }) ++
        (u.oAuth2Info match {
          case None => BSONDocument()
          case Some(o) => BSONDocument("oAuth1Info" ->
            BSONDocument("accessToken" -> o.accessToken, "tokenType" -> o.tokenType))
        }) ++
        (u.passwordInfo match {
          case None => BSONDocument()
          case Some(p) => BSONDocument("passwordInfo" -> (
            BSONDocument("hasher" -> p.hasher, "password" -> p.password) ++
              (p.salt match {
                case None => BSONDocument()
                case Some(s) => BSONDocument("salt" -> s)
              })))
        })
  }

}