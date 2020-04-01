import sbt._, Keys._

ThisBuild / organization := "com.slamdata"

ThisBuild / homepage := Some(url("https://github.com/precog/scala-pathy"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/precog/scala-pathy"),
    "scm:git@github.com:precog/scala-pathy.git"))

ThisBuild / githubRepository := "scala-pathy"
ThisBuild / publishAsOSSProject := true

ThisBuild / crossScalaVersions := Seq("2.13.1", "2.12.11", "2.11.12")
ThisBuild / scalaVersion := (ThisBuild / crossScalaVersions).value.head

lazy val argonautVersion     = "6.2.3"
lazy val scalazVersion       = "7.2.30"
lazy val scalacheckVersion   = "1.14.3"
lazy val specsVersion        = "4.8.2"

lazy val baseSettings = commonBuildSettings ++ Seq(
  libraryDependencies += "com.slamdata" %% "slamdata-predef" % "0.1.2"
)

lazy val allSettings = baseSettings

lazy val root = (project in file("."))
  .aggregate(core, argonaut, scalacheck, tests)
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(Seq(
    name := "pathy"
  ))

lazy val core = (project in file("core"))
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
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(Seq(
    name := "pathy-tests",
    libraryDependencies ++= Seq(
      "org.scalaz"     %% "scalaz-core"               % scalazVersion                        % Test,
      "org.scalaz"     %% "scalaz-scalacheck-binding" % (scalazVersion + "-scalacheck-1.14") % Test,
      "org.scalacheck" %% "scalacheck"                % scalacheckVersion                    % Test,
      "org.specs2"     %% "specs2-core"               % specsVersion                         % Test,
      "org.specs2"     %% "specs2-scalacheck"         % specsVersion                         % Test
    )
  ))
