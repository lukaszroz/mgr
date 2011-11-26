package edu.agh.lroza.locks

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CustomLocksServerScalaTest extends FunSuite with FunSuiteServerBeahaviors {
  def server = CustomLocksServerScala();

  basicLogInLogOut(server)
  noticesManagement(server)
}