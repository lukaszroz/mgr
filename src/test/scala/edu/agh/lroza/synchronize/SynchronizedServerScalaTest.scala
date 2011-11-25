package edu.agh.lroza.synchronize

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors

class SynchronizedServerScalaTest extends FunSuite with FunSuiteServerBeahaviors {
  def server = SynchronizedServerScala();

  basicLogInLogOut(server)
  noticesManagement(server)
}