package edu.agh.lroza.synchronize

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SynchronizedServerScalaTest extends FunSuite with FunSuiteServerBeahaviors {
  def server = new SynchronizedServerScala();

  basicLogInLogOut(server)
  noticesManagement(server)
}