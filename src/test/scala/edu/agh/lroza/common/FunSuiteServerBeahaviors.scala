package edu.agh.lroza.common

import java.util.UUID
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite


trait FunSuiteServerBeahaviors extends ShouldMatchers {
  this: FunSuite =>

  def basicLogInLogOut(serverFactory: => NoticeBoardServer) {
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
      serverFactory.listNoticesIds(UUID.randomUUID) should be('left)
    }

    test("should not be able to add topic without login") {
      serverFactory.addNotice(UUID.randomUUID, "topic1", "message1") should be('left)
    }

    test("should list topics after login") {
      val server = serverFactory
      val token = server.login("a", "a").get
      server.listNoticesIds(token) should be('right)
    }

    test("should not be able to list topics after logout") {
      val server = serverFactory
      val token = server.login("a", "a").get
      server.logout(token)
      server.listNoticesIds(token) should be('left)
    }
  }

  def topicsManagement(serverFactory: => NoticeBoardServer) {
    test("should be able to add, get, update and remove topic") {
      val server = serverFactory
      val title = "title"
      val secondTitle = "title2"
      val message = "message"

      val token = server.login("a", "a").get
      //should be able to add topic
      var topic = server.addNotice(token, title, message)
      topic should be('right)

      //should not be able to add topic with the same title
      server.addNotice(token, title, message) should be('left)

      server.listNoticesIds(token).right.get sameElements Iterable(title) should be(true)

      //should be able to get topic
      server.getNotice(token, title).right.get should equal(topic.right.get)

      //should return Problem when asked for Topic not present
      server.getNotice(token, secondTitle) should be('left)

      topic = server.updateTopicTitle(token, title, secondTitle)
      topic should be('right)
      //should not be able to update when there is no such title
      server.updateTopicTitle(token, title, secondTitle) should be('left)

      server.listNoticesIds(token).right.get sameElements Iterable(secondTitle) should be(true)
      server.getNotice(token, secondTitle).right.get should equal(topic.right.get)

      server.addNotice(token, title, message) should be('right)
      server.listNoticesIds(token).right.get sameElements Iterable(secondTitle, title) should be(true)
      //should not be able to change title to existing one
      server.updateTopicTitle(token, title, secondTitle) should be('left)

      //should be able to delete topic
      server.deleteNotice(token, title) should equal(true)
      server.deleteNotice(token, secondTitle) should equal(true)
      server.listNoticesIds(token).right.get sameElements Set() should be(true)

      //shouldn't be able to delete when there is no topic
      server.deleteNotice(token, title) should equal(false)

      //tests after logout
      server.addNotice(token, title, message) should be('right)
      server.logout(token)
      server.addNotice(token, secondTitle, message) should be('left)
      server.getNotice(token, title) should be('left)
      server.updateTopicTitle(token, title, secondTitle) should be('left)
      server.deleteNotice(token, title) should be(false)
    }
  }
}