package edu.agh.lroza.common

import java.util.UUID
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import java.util.concurrent.CountDownLatch
import actors.threadpool.{TimeUnit, Executors}

trait FunSuiteServerBeahaviors extends ShouldMatchers {
  this: FunSuite =>

  def basicLogInLogOut(serverFactory: => NoticeBoardServer) {
    test("should not be able to login with wrong credentials") {
      serverFactory.login("a", "b") should be('left)
    }

    test("should not be able to logout with wrong token") {
      serverFactory.logout(UUID.randomUUID()) should be('defined)
    }

    test("should be able to login and then logout") {
      val server = serverFactory
      val token = server.login("a", "a").right.get
      server.logout(token) should equal(None)
    }

    test("should not be able to list notices without login") {
      val listNoticesIds = serverFactory.listNoticesIds(UUID.randomUUID)
      listNoticesIds should be('left)
    }

    test("should not be able to add notice without login") {
      serverFactory.addNotice(UUID.randomUUID, "notice1", "message1") should be('left)
    }

    test("should list notices after login") {
      val server = serverFactory
      val token = server.login("a", "a").right.get
      val listNoticesIds = server.listNoticesIds(token)
      listNoticesIds should be('right)
    }

    test("should not be able to list notices after logout") {
      val server = serverFactory
      val token = server.login("a", "a").right.get
      server.listNoticesIds(token) should be('right)
      server.logout(token)
      server.listNoticesIds(token) should be('left)
    }
  }

  def noticesManagement(serverFactory: => NoticeBoardServer) {
    test("should be able to add, get, update and remove notice") {
      val server = serverFactory
      val title = "title"
      val secondTitle = "title2"
      val message = "message"

      val token = server.login("a", "a").right.get
      //should be able to add notice
      var either = server.addNotice(token, title, message)
      either should be('right)
      var id = either.right.get

      //should not be able to add notice with the same title
      server.addNotice(token, title, message + "2") should be('left)

      server.listNoticesIds(token).right.get should (contain(id) and have size (1))

      server.getNotice(token, id).right.get should have('title(title), 'message(message))

      //should return Problem when asked for notice not present
      val wrongId = new Id {
        def id = title + secondTitle
      }
      server.getNotice(token, wrongId) should be('left)

      either = server.updateNotice(token, id, secondTitle, message)
      either should be('right)
      id = either.right.get

      //updated id should be returned (may be the same)
      server.listNoticesIds(token).right.get should (contain(id) and have size (1))
      //notice should be updated
      server.getNotice(token, id).right.get should have('title(secondTitle), 'message(message))

      //should be able to get notice
      either = server.addNotice(token, title, message)
      either should be('right)
      val id2 = either.right.get
      server.listNoticesIds(token).right.get should (contain(id) and contain(id2) and have size (2))
      //should not be able to change title to existing one
      server.updateNotice(token, id2, secondTitle, message) should be('left)
      //should be able to change only message
      server.updateNotice(token, id2, title, message + "22") should be('right)
      server.getNotice(token, id2).right.get should have('title(title), 'message(message + "22"))

      //should be able to delete notice
      server.deleteNotice(token, id2) should equal(None)
      server.deleteNotice(token, id) should equal(None)
      server.listNoticesIds(token).right.get should be('empty)

      //shouldn't be able to delete when there is no notice
      server.deleteNotice(token, id) should be('defined)

      //shouldn't be able to update non existing notice
      server.updateNotice(token, id, title, message) should be('left)

      //shouldn't be able to get non existing notice
      server.getNotice(token, id) should be('left)

      //should be able to add notice after failed update
      either = server.addNotice(token, title, message)
      either should be('right)
      id = either.right.get

      //tests after logout
      server.logout(token)
      server.addNotice(token, secondTitle, message) should be('left)
      server.getNotice(token, id) should be('left)
      server.updateNotice(token, id, secondTitle, message) should be('left)
      server.deleteNotice(token, id) should be('defined)
    }

    test("id set should be snapshot") {
      val server = serverFactory
      val token = server.login("a", "a").right.get
      for (i <- 1 to 100) {
        server.addNotice(token, "title" + i, "message" + i)
      }
      val set = server.listNoticesIds(token).right.get;
      val n = 10
      val pool = Executors.newFixedThreadPool(n)
      val cdlStart = new CountDownLatch(1);
      val cdlStop = new CountDownLatch(n * 2);
      for (i <- 1 to n * 2) {
        pool.submit(new Runnable {
          def run() {
            cdlStart.await();
            Thread.sleep(10)
            try {
              val id = server.addNotice(token, "title0" + i, "message0" + i).right.get
            } finally {
              cdlStop.countDown()
            }
          }
        })
      }
      try {
        cdlStart.countDown()
        var c = 0
        for (id <- set) {
          Thread.sleep(1)
          c += 1
        }
        set should have size (100)
        c should be(100)
        cdlStop.await()
        val newSet = server.listNoticesIds(token).right.get;
        newSet should have size (100 + n * 2)
      } finally {
        pool.shutdown()
        pool.awaitTermination(10, TimeUnit.SECONDS)
      }
    }
  }
}