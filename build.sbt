val scala3Version = "3.3.1"
val zioVersion = "2.0.19"

ThisBuild / scalaVersion := scala3Version
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"

// Enable scalafix
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val root = project
  .in(file("."))
  .settings(
    name := "gemini-zio-project",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-http" % "3.0.0-RC4",
      "dev.zio" %% "zio-json" % "0.6.2",
      "com.google.cloud" % "google-cloud-vertexai" % "0.2.0",
      "com.typesafe" % "config" % "1.4.3",
      
      // Test dependencies
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  ) 