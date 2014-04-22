name := "adminphoto"

version := "1.0"

scalacOptions ++= Seq("-feature")

libraryDependencies ++= Seq(
  cache,
  "commons-io" % "commons-io"% "2.4",
  "commons-net" % "commons-net"% "3.3",
  "org.scalatest" % "scalatest_2.10" % "2.0",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2",
  "ws.securesocial" %% "securesocial" % "2.1.3"
)

play.Project.playScalaSettings

resolvers += Resolver.sonatypeRepo("releases")

com.jamesward.play.BrowserNotifierPlugin.livereload

