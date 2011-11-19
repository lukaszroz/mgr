package edu.agh.lroza.actors

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors

class ActorServerJavaTest extends FunSuite with FunSuiteServerBeahaviors {
  val server = new ActorServerJava();

  basicLogInLogOut(server)
}