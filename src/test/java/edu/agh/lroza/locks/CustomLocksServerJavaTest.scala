package edu.agh.lroza.locks

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors

class CustomLocksServerJavaTest extends FunSuite with FunSuiteServerBeahaviors {
  val server = new CustomLocksServerJava();

  basicLogInLogOut(server)
  noticesManagement(server)
}