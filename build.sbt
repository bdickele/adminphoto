name := "adminphoto"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  cache,
  "org.scalatest" % "scalatest_2.10" % "2.0",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2"
)

play.Project.playScalaSettings

