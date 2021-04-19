name := "railinfo"
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(
  guice,
  "org.mongodb" % "mongo-java-driver" % "3.5.0",
  "org.mongodb.morphia" % "morphia" % "1.3.2"
)
