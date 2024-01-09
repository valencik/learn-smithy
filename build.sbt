ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version := "0.1.0-SNAPSHOT"

val caseInsensitiveVersion = "1.4.0"
val catsEffectVersion = "3.5.2"
val catsVersion = "2.10.0"
val fs2Version = "3.9.3"
val http4sVersion = "0.23.25"
val ip4sVersion = "3.4.0"
val logbackVersion = "1.4.14"
val scribeVersion = "3.13.0"

lazy val root = tlCrossRootProject.aggregate(common, init)

lazy val common = (project in file("00-common/"))
  .settings(
    name := "00-common",
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-io" % fs2Version,
      "com.comcast" %% "ip4s-core" % ip4sVersion,
      "com.disneystreaming.smithy4s" %% "smithy4s-core" % smithy4sVersion.value,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s-swagger" % smithy4sVersion.value,
      "com.outr" %% "scribe-slf4j2" % scribeVersion % Runtime,
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
