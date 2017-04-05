import sbt._, Keys._

import slamdata.CommonDependencies
import slamdata.SbtSlamData.transferPublishAndTagResources

lazy val baseSettings = commonBuildSettings ++ Seq(
  organization := "com.slamdata",
  libraryDependencies += CommonDependencies.slamdata.predef
)

lazy val publishSettings = commonPublishSettings ++ Seq(
  organizationName := "SlamData Inc.",
  organizationHomepage := Some(url("http://slamdata.com")),
  homepage := Some(url("https://github.com/slamdata/scala-pathy")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/slamdata/scala-pathy"),
      "scm:git@github.com:slamdata/scala-pathy.git"
    )
  ))

lazy val allSettings =
  baseSettings ++ publishSettings

lazy val root = (project in file("."))
  .aggregate(core, argonaut, scalacheck, tests)
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(transferPublishAndTagResources)
  .settings(Seq(
    name := "pathy"
  ))

lazy val core = (project in file("core"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(allSettings)
  .settings(Seq(
    name := "pathy-core",
    initialCommands in console := "import pathy._, Path._",
    libraryDependencies += CommonDependencies.scalaz.core
  ))

lazy val argonaut = (project in file("argonaut"))
  .dependsOn(core, scalacheck)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(allSettings)
  .settings(Seq(
    name := "pathy-argonaut",
    libraryDependencies ++= Seq(
      CommonDependencies.argonaut.argonaut,
      CommonDependencies.specs2.core       % Test,
      CommonDependencies.specs2.scalacheck % Test
    )
  ))

lazy val scalacheck = (project in file("scalacheck"))
  .dependsOn(core)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(allSettings)
  .settings(Seq(
    name := "pathy-scalacheck",
    libraryDependencies ++= Seq(
      CommonDependencies.scalaz.core,
      CommonDependencies.scalacheck.scalacheck
    )
  ))

lazy val specs2 = (project in file("specs2"))
  .dependsOn(scalacheck)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(allSettings)
  .settings(Seq(
    name := "pathy-specs2",
    libraryDependencies ++= Seq(
      CommonDependencies.specs2.core,
      CommonDependencies.specs2.scalacheck
    )
  ))

lazy val tests = (project in file("tests"))
  .dependsOn(core, scalacheck, specs2)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(Seq(
    name := "pathy-tests",
    libraryDependencies ++= Seq(
      CommonDependencies.scalaz.core              % Test,
      CommonDependencies.specs2.core              % Test,
      CommonDependencies.specs2.scalacheck        % Test,
      CommonDependencies.scalacheck.scalacheck    % Test,
      CommonDependencies.scalaz.scalacheckBinding % Test
    )
  ))
