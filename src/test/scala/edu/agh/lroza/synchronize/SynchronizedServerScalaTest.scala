package edu.agh.lroza.synchronize

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors

class SynchronizedServerScalaTest extends FunSuite with FunSuiteServerBeahaviors {
  val server = SynchronizedServerScala();

  basicLogInLogOut(server)
}