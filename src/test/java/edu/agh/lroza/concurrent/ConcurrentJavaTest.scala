package edu.agh.lroza.concurrent

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import edu.agh.lroza.NoticeBoardServerJavaWrapper

@RunWith(classOf[JUnitRunner])
class ConcurrentJavaTest extends FunSuite with FunSuiteServerBeahaviors {
  def server = new NoticeBoardServerJavaWrapper(new ConcurrentServerJava());

  basicLogInLogOut(server)
  noticesManagement(server)
}