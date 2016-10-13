import play.Keys._
import play.PlayScala
import sbt._
import sbt.Keys._

name := """dbservice2"""

version := "2.0"

//resolvers ++= Seq(
//  "E.On ECT Nexus" at "http://172.18.32.140:8081/nexus/content/groups/public/",
//  "E.On Snapshots" at "http://172.18.32.11/maven/snapshots/",
//  "E.On Releases" at "http://172.18.32.11/maven/releases/"
//)

libraryDependencies ++= Seq(
//  "org.webjars" %% "webjars-play" % "2.3-M1",
//  "org.webjars" % "bootstrap" % "2.3.1",
//  "org.webjars" % "requirejs" % "2.1.11-1",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "org.mockito" % "mockito-core" % "1.9.5" % "test",
//  "com.wordnik" %% "swagger-play2" % "1.3.7",
//  "eon" %% "eon-swagger" % "0.2-SNAPSHOT",
  jdbc
)

routesImport ++= Seq("scala.language.reflectiveCalls")

lazy val root = (project in file(".")).addPlugins(PlayScala)
