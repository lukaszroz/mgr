package edu.agh.lroza.common

import java.util.UUID
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite


trait FunSuiteServerBeahaviors extends ShouldMatchers {
  this: FunSuite =>

  def basicLogInLogOut(serverFactory: => Server) {
    test("should not be able to login with wrong credentials") {
      serverFactory.login("a", "b") should equal(None)
    }

    test("should not be able to logout with wrong token") {
      serverFactory.logout(UUID.randomUUID()) should equal(false)
    }

    test("should be able to login and then logout") {
      val server = serverFactory
      val token = server.login("a", "a").get
      server.logout(token) should equal(true)
    }

    test("should not be able to list topics without login") {
      serverFactory.listTopics(UUID.randomUUID) should be('left)
    }

    test("should not be able to add topic without login") {
      serverFactory.addTopic(UUID.randomUUID, "topic1", "message1") should be('left)
    }

    test("should list topics after login") {
      val server = serverFactory
      val token = server.login("a", "a").get
      server.listTopics(token) should be('right)
    }

    test("should not be able to list topics after logout") {
      val server = serverFactory
      val token = server.login("a", "a").get
      server.logout(token)
      server.listTopics(token) should be('left)
    }
  }

  def topicsListManagement(serverFactory: => Server) {
    test("should be able to add, get, update and remove topic") {
      val server = serverFactory
      val title = "title"
      val secondTitle = "title2"
      val message = "message"

      val token = server.login("a", "a").get
      var topic = server.addTopic(token, title, message)
      topic should be('right)

      //should not be able to add topic with the same title
      server.addTopic(token, title, message) should be('left)

      server.listTopics(token).right.get sameElements Iterable(title) should be(true)

      server.getTopic(token, title).right.get should equal(topic.right.get)
      //should return Problem when asked for Topic not present
      server.getTopic(token, secondTitle) should be('left)

      topic = server.updateTopic(token, title, secondTitle)
      topic should be('right)
      //should not be able to update when there is no such title
      server.updateTopic(token, title, secondTitle) should be('left)

      server.listTopics(token).right.get sameElements Iterable(secondTitle) should be(true)
      server.getTopic(token, secondTitle).right.get should equal(topic.right.get)

      server.addTopic(token, title, message) should be('right)
      server.listTopics(token).right.get sameElements Iterable(secondTitle, title) should be(true)
      //should not be able to change title to existing one
      server.updateTopic(token, title, secondTitle) should be('left)
    }
  }
}