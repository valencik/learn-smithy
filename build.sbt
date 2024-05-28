ThisBuild / organization := "fff.pink"
ThisBuild / organizationName := "Andrew Valencik"
ThisBuild / startYear := Some(2023)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("valencik", "Andrew Valencik")
)

// do not publish artifacts
ThisBuild / githubWorkflowPublishTargetBranches := Seq()

// use JDK 17
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))

ThisBuild / tlBaseVersion := "0.0"
ThisBuild / scalaVersion := "2.13.14"

val caseInsensitiveVersion = "1.4.0"
val catsEffectVersion = "3.5.4"
val catsVersion = "2.12.0"
val fs2Version = "3.10.2"
val http4sVersion = "0.23.27"
val ip4sVersion = "3.5.0"
val logbackVersion = "1.4.14"
val scribeVersion = "3.13.5"

lazy val root = tlCrossRootProject.aggregate(common, init, basic, searchSchema)

lazy val common = (project in file("00-common/"))
  .settings(
    name := "00-common",
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-io" % fs2Version,
      "com.comcast" %% "ip4s-core" % ip4sVersion,
      "com.disneystreaming.smithy4s" %% "smithy4s-core" % smithy4sVersion.value,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s-swagger" % smithy4sVersion.value,
      "com.outr" %% "scribe-slf4j2" % scribeVersion,
      "org.http4s" %% "http4s-core" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-server" % http4sVersion,
      "org.typelevel" %% "case-insensitive" % caseInsensitiveVersion,
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.typelevel" %% "cats-effect-kernel" % catsEffectVersion
    )
  )

lazy val init = (project in file("01-init/"))
  .enablePlugins(Smithy4sCodegenPlugin)
  .dependsOn(common)
  .settings(
    name := "01-init",
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %% "smithy4s-core" % smithy4sVersion.value,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion.value,
      "org.http4s" %% "http4s-core" % http4sVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.typelevel" %% "cats-effect-kernel" % catsEffectVersion
    ),
    Compile / run / fork := true,
    Compile / run / connectInput := true
  )

lazy val basic = (project in file("02-basic/"))
  .enablePlugins(Smithy4sCodegenPlugin)
  .dependsOn(common)
  .settings(
    name := "01-init",
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %% "smithy4s-core" % smithy4sVersion.value,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion.value,
      "org.http4s" %% "http4s-core" % http4sVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.typelevel" %% "cats-effect-kernel" % catsEffectVersion
    ),
    Compile / run / fork := true,
    Compile / run / connectInput := true
  )

lazy val searchSchema = (project in file("03-search-schema/"))
  .enablePlugins(Smithy4sCodegenPlugin)
  .dependsOn(common)
  .settings(
    name := "01-init",
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %% "smithy4s-core" % smithy4sVersion.value,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion.value,
      "org.http4s" %% "http4s-core" % http4sVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.typelevel" %% "cats-effect-kernel" % catsEffectVersion
    ),
    Compile / run / fork := true,
    Compile / run / connectInput := true
  )
