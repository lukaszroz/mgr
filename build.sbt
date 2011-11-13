import AssemblyKeys._ // put this at the top of the file

seq(assemblySettings: _*)

//skip this project sources
//assembleArtifact in packageBin := false

test in assembly := {}

name := "hello"

version := "1.0"

scalaVersion := "2.9.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.6.1" % "test",
  "com.novocode" % "junit-interface" % "0.7" % "test->default",
  "se.scalablesolutions.akka" % "akka-remote" % "1.2",
  "se.scalablesolutions.akka" % "akka-typed-actor" % "1.2"
)