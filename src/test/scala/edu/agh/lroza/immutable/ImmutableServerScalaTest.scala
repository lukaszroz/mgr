package edu.agh.lroza.immutable

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ImmutableServerScalaTest extends FunSuite with FunSuiteServerBeahaviors {
  def server = ImmutableServerScala();

  basicLogInLogOut(server)
  noticesManagement(server)
}