package edu.agh.lroza.locks

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors

class CustomLocksServerScalaTest extends FunSuite with FunSuiteServerBeahaviors {
  def server = CustomLocksServerScala();

  basicLogInLogOut(server)
  noticesManagement(server)
}