val scala3Version = "3.3.1"
val zioVersion = "2.0.19"
val zioHttpVersion = "3.0.0-RC2"
val zioJsonVersion = "0.6.2"

ThisBuild / scalaVersion := scala3Version
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"

// Enable scalafix
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

coverageMinimumStmtTotal := 80
coverageFailOnMinimum := true
coverageHighlighting := true

lazy val root = project
  .in(file("."))
  .settings(
    name := "gemini-zio-project",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-http" % zioHttpVersion,
      "dev.zio" %% "zio-json" % zioJsonVersion,
      "com.google.cloud" % "google-cloud-vertexai" % "0.2.0",
      "com.typesafe" % "config" % "1.4.3",
      
      // Test dependencies
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test,
      "com.github.tomakehurst" % "wiremock-jre8" % "2.35.1" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  ) 