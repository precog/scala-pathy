import sbt._, Keys._

import slamdata.SbtSlamData.transferPublishAndTagResources

lazy val argonautVersion     = "6.2"
lazy val scalazVersion       = "7.2.15"
lazy val scalacheckVersion   = "1.13.4"
lazy val specsVersion        = "3.8.6"

lazy val baseSettings = commonBuildSettings ++ Seq(
  organization := "com.slamdata",
  libraryDependencies += "com.slamdata" %% "slamdata-predef" % "0.0.6"
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
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core" % scalazVersion
    )
  ))

lazy val argonaut = (project in file("argonaut"))
  .dependsOn(core, scalacheck)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(allSettings)
  .settings(Seq(
    name := "pathy-argonaut",
    libraryDependencies ++= Seq(
      "io.argonaut" %% "argonaut"          % argonautVersion,
      "org.specs2"  %% "specs2-core"       % specsVersion % Test,
      "org.specs2"  %% "specs2-scalacheck" % specsVersion % Test
    )
  ))

lazy val scalacheck = (project in file("scalacheck"))
  .dependsOn(core)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(allSettings)
  .settings(Seq(
    name := "pathy-scalacheck",
    libraryDependencies ++= Seq(
      "org.scalaz"     %% "scalaz-core" % scalazVersion,
      "org.scalacheck" %% "scalacheck"  % scalacheckVersion
    )
  ))

lazy val specs2 = (project in file("specs2"))
  .dependsOn(scalacheck)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(allSettings)
  .settings(Seq(
    name := "pathy-specs2",
    libraryDependencies ++= Seq(
      "org.specs2"  %% "specs2-core"       % specsVersion,
      "org.specs2"  %% "specs2-scalacheck" % specsVersion
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
      "org.scalaz"     %% "scalaz-core"               % scalazVersion                        % Test,
      "org.scalaz"     %% "scalaz-scalacheck-binding" % (scalazVersion + "-scalacheck-1.13") % Test,
      "org.scalacheck" %% "scalacheck"                % scalacheckVersion                    % Test,
      "org.specs2"     %% "specs2-core"               % specsVersion                         % Test,
      "org.specs2"     %% "specs2-scalacheck"         % specsVersion                         % Test
    )
  ))
