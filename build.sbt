name := "railinfo"
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.14"

libraryDependencies ++= Seq(
  guice,
  "org.mongodb" % "mongodb-driver-sync" % "4.11.2",
  "dev.morphia.morphia" % "morphia-core" % "2.4.14",
  "commons-codec" % "commons-codec" % "1.15"
)
