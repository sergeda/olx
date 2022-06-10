ThisBuild / scalaVersion     := "3.1.1"
ThisBuild / version          := "0.2"
ThisBuild / organization     := "serg.dashko"
ThisBuild / organizationName := "dashko"

lazy val zioVersion = "2.0.0-RC5"
lazy val sttpVersion = "3.5.2"
lazy val circeVersion = "0.14.1"

lazy val root = (project in file("."))
  .settings(
    name := "olx",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "com.softwaremill.sttp.client3" %% "httpclient-backend-zio" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "circe" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "slf4j-backend" % sttpVersion,
      "dev.zio" %% "zio-cache" % "0.2.0-RC5",
      "dev.zio" %% "zio-logging" % zioVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "dev.zio" %% "zio-prelude" % "1.0.0-RC13",
      "dev.zio" %% "zio-interop-cats" % "3.3.0-RC6",
      "is.cir" %% "ciris" % "2.3.2",
      "lt.dvim.ciris-hocon" %% "ciris-hocon" % "1.0.1",
      "dev.zio" %% "zio-test" % zioVersion % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

enablePlugins(JavaAppPackaging)
