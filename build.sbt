import AssemblyKeys._

seq(assemblySettings: _*)

jarName := "simulation.jar"

mainClass in assembly := Some("edu.agh.lroza.simulation.Simulation")

test in assembly := {}

name := "simulation"

version := "1.0"

scalaVersion := "2.9.1"

scalacOptions ++= Seq("-unchecked", "-deprecation")

javacOptions ++= Seq("-Xlint")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

parallelExecution in IntegrationTest := false

libraryDependencies ++= Seq(
  "se.scalablesolutions.akka" % "akka-actor" % "1.2",
  "org.mockito" % "mockito-core" % "1.8.5",
  "com.google.guava" % "guava" % "10.0.1",
  "org.clapper" %% "argot" % "0.3.5",
  "junit" % "junit" % "4.8" % "test",
  "org.scalatest" %% "scalatest" % "1.6.1" % "test",
  "com.novocode" % "junit-interface" % "0.7" % "test"
)