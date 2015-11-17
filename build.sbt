import sbt._
import Keys._
import de.heikoseeberger.sbtheader.license.Apache2_0

lazy val buildSettings = Seq(
  organization := "com.slamdata",
  scalaVersion := "2.11.7",
  crossScalaVersions := Seq("2.10.5", "2.11.7")
)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
  "-Xfuture"
)

lazy val baseSettings = Seq(
  scalacOptions ++= compilerOptions ++ (
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 11)) => Seq("-Ywarn-unused-import")
      case _             => Nil
    }
  ),
  scalacOptions in (Compile, console) := compilerOptions,
  headers := Map("scala" -> Apache2_0("2014 - 2015", "SlamData Inc."))
)

// Supply the absolute path to release credentials file via RELEASE_CREDS env var
lazy val releaseCreds: Seq[Credentials] =
  Option(System.getenv("RELEASE_CREDS"))
    .map(path => Credentials(new File(path)))
    .toSeq

/**
 * Set PGP_KEY_DIR to the absolute path of the directory containing the
 * "secring.pgp" and "pubring.pgp" keyrings to use for signing artifacts.
 *
 * Set PGP_PASSPHRASE to the passphrase to unlock the private pgp signing key
 *
 * Both options are optional, omiting the directory will result in the default
 * locations being used and omitting the passphrase will result in sbt prompting
 * for it.
 */
lazy val pgpSettings = {
  val keyrings = Option(System.getenv("PGP_KEY_DIR")).toSeq.flatMap(dir => Seq(
    pgpSecretRing := (file(dir) / "secring.pgp"),
    pgpPublicRing := (file(dir) / "pubring.pgp")
  ))

  val passphrase = Option(System.getenv("PGP_PASSPHRASE"))
    .map(s => pgpPassphrase := Some(s.toArray))

  keyrings ++ passphrase.toSeq
}

lazy val publishSettings = Seq(
  organizationName := "SlamData Inc.",
  organizationHomepage := Some(url("http://slamdata.com")),
  homepage := Some(url("https://github.com/slamdata/scala-pathy")),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  credentials ++= releaseCreds,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseCrossBuild := true,
  autoAPIMappings := true,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/slamdata/scala-pathy"),
      "scm:git@github.com:slamdata/scala-pathy.git"
    )
  ),
  developers := List(Developer(id = "slamdata",
                               name = "SlamData Inc.",
                               email = "contact@slamdata.com",
                               url = new URL("http://slamdata.com")))
)

lazy val allSettings = buildSettings ++ baseSettings ++ publishSettings ++ pgpSettings

lazy val root = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(allSettings)
  .settings(Seq(
    name := "pathy",
    initialCommands in console := "import pathy._, Path._",
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core" % "7.1.0",
      "org.specs2" %% "specs2-core" % "3.6.4" % "test",
      "org.specs2" %% "specs2-scalacheck" % "3.6.4" % "test",
      "org.scalacheck" %% "scalacheck" % "1.12.5" % "test"
    )
  ))
