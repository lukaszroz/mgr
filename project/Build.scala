import sbt._
import Keys._

object Build extends Build {
  lazy val root =
    Project("root", file("."))
      .configs(IntegrationTest)
      .settings(Defaults.itSettings: _*)
      .settings(libraryDependencies ++= Seq(specs, junit))

  lazy val specs = "org.scalatest" %% "scalatest" % "1.6.1" % "it,test"
  lazy val junit = "com.novocode" % "junit-interface" % "0.7" % "it,test"
}
