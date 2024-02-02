name := "railinfo"
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(
  guice,
  "org.mongodb" % "mongodb-driver-sync" % "4.11.1",
  "dev.morphia.morphia" % "morphia-core" % "2.4.11",
  "commons-codec" % "commons-codec" % "1.15"
)
