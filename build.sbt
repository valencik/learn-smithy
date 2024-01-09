ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version := "0.1.0-SNAPSHOT"

val http4sVersion = "0.23.25"
val logbackVersion = "1.4.14"

lazy val root = tlCrossRootProject.aggregate(init)

lazy val init = (project in file("01-init/"))
  .enablePlugins(Smithy4sCodegenPlugin)
  .settings(
    name := "01-init",
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion.value,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s-swagger" % smithy4sVersion.value,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion % Runtime
    ),
    Compile / run / fork := true,
    Compile / run / connectInput := true
  )
