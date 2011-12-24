package edu.agh.lroza.actors

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import edu.agh.lroza.NoticeBoardServerJavaWrapper

@RunWith(classOf[JUnitRunner])
class ActorServerJavaTest extends FunSuite with FunSuiteServerBeahaviors {
  def server = new NoticeBoardServerJavaWrapper(new ActorServerJava());

  basicLogInLogOut(server)
  noticesManagement(server)
}