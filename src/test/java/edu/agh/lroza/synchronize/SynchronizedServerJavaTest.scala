package edu.agh.lroza.synchronize

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors

class SynchronizedServerJavaTest extends FunSuite with FunSuiteServerBeahaviors {
  val server = new SynchronizedServerJava();

  basicLogInLogOut(server)
}