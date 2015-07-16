import sbt._
import Keys._
import de.heikoseeberger.sbtheader.license.Apache2_0
import de.heikoseeberger.sbtheader.HeaderPlugin

// headers := Map(
//   "scala" -> Apache2_0("2014 - 2015", "SlamData Inc."),
//   "java"  -> Apache2_0("2014 - 2015", "SlamData Inc."))

organization := "slamdata"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.scalaz"        %% "scalaz-core"       % "7.1.0",
  "org.specs2"        %% "specs2-core"       % "2.3.13-scalaz-7.1.0-RC1" % "test"
)
