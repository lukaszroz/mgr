import AssemblyKeys._

// put this at the top of the file

seq(assemblySettings: _*)

//skip this project sources
//assembleArtifact in packageBin := false

test in assembly := {}

name := "hello"

version := "1.0"

scalaVersion := "2.9.1"

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

parallelExecution in IntegrationTest := false

libraryDependencies ++= Seq(
  "se.scalablesolutions.akka" % "akka-actor" % "1.2",
  "org.mockito" % "mockito-core" % "1.8.5"
)