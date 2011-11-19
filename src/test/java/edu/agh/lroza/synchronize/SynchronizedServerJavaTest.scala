package edu.agh.lroza.synchronize

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import java.util.UUID

class SynchronizedServerJavaTest extends FunSuite with ShouldMatchers {
  val server = new SynchronizedServerJava();

  test("should not be able to login with wrong credentials") {
    server.login("a", "b") should equal(None)
  }

  test("should not be able to logout with wrong token") {
    server.logout(UUID.randomUUID()) should equal(false)
  }

  test("should be able to login and then logout") {
    val token = server.login("a", "a").get
    server.logout(token) should equal(true)
  }

  test("should not be able to list topics without login") {
    server.listTopics(null) should be('left)
  }

  test("should list topics after login") {
    val token = server.login("a", "a").get
    server.listTopics(token) should be('right)
  }

  test("should not be able to list topics after logout") {
    val token = server.login("a", "a").get
    server.logout(token)
    server.listTopics(token) should be('left
    )
  }
}