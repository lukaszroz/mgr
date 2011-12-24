package edu.agh.lroza.locks

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import edu.agh.lroza.NoticeBoardServerJavaWrapper

@RunWith(classOf[JUnitRunner])
class CustomLocksServerJavaTest extends FunSuite with FunSuiteServerBeahaviors {
  def server = new NoticeBoardServerJavaWrapper(new CustomLocksServerJava());

  basicLogInLogOut(server)
  noticesManagement(server)
}