package edu.agh.lroza.actors

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ActorServerScalaTest extends FunSuite with FunSuiteServerBeahaviors {
  def server = ActorServerScala();

  basicLogInLogOut(server)
  noticesManagement(server)
}