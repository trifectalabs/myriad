name := "myriad"

organization := "trifectalabs"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4-M3",
  "com.typesafe.akka" %% "akka-testkit" % "2.4-M3",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)
