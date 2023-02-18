ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

lazy val AkkaVersion = "2.6.19"
lazy val ScalaTestVersion = "3.2.11"
lazy val LogbackVersion = "1.2.11"
lazy val AkkaHttpVersion = "10.2.9"
lazy val PureConfigVersion = "0.17.1"


lazy val root = (project in file("."))
  .settings(
    name := "akka-http-stream-collatz",
    fork := true,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,

      "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,

      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,

      "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
      "org.scalatest" %% "scalatest-propspec" % ScalaTestVersion % Test
    )
  )
