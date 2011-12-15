package edu.agh.lroza.common

import java.util.UUID
import actors.Actor
import edu.agh.lroza.common.User.{Stop, Run}
import java.util.concurrent.{CyclicBarrier, TimeUnit}


class User(server: NoticeBoardServer, number: Int, barrier: CyclicBarrier, writeEvery: Int) extends Actor {
  var token = UUID.randomUUID()
  val logPrefix = "[client%2d]".format(number)
  var size, startTime, duration, count, problemCount = 0L
  var currentId: Id = null
  val shift = if (writeEvery == 0) 0 else number * 11 % writeEvery

  var nextWriteAction: () => Unit = null

  def problem(p: Problem) = {
    problemCount += 1
    -1
  }

  val addNotice: () => Unit = () => {
    addCount += 1
    size += server.addNotice(token, getTitle(addCount), "message" + logPrefix).fold(problem, _.toString.length())
    postCall()
    nextWriteAction = updateNotice
  }

  val updateNotice: () => Unit = () => {
    updateCount += 1
    size += server.updateNotice(token, currentId, getTitle(updateCount), "message" + logPrefix)
      .fold(problem, _.toString.length())
    postCall()
    nextWriteAction = deleteNotice
  }

  var addCount = 0L
  var updateCount = 0L

  def getTitle(cout: Long) = {
    if (count % 10 == 0) {
      "testTitle%05d".format(count)
    } else {
      "title%s%07d".format(logPrefix, count)
    }
  }

  val deleteNotice: () => Unit = () => {
    size += server.deleteNotice(token, currentId).map(problem(_)).getOrElse(1)
    postCall()
    nextWriteAction = addNotice
  }

  nextWriteAction = addNotice

  def reset() {
    duration = 0L
    count = 0L
    problemCount = 0L
    addCount = 0L
    updateCount = 0L
  }

  def postCall() {
    count += 1
    if (count % 100 == 50) {
      server.logout(token).foreach(_ => problemCount += 1)
      token = server.login(logPrefix, logPrefix).right.get
      postCall()
      postCall()
    }

    if (writeEvery > 1 && count % writeEvery == shift) {
      nextWriteAction()
      postCall()
    }
  }

  def end() {
    server.logout(token).foreach(_ => problemCount += 1)
    postCall()
    duration += TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime)
    if (writeEvery > 1) {
      while (nextWriteAction != addNotice) {
        nextWriteAction()
      }
    }
  }

  def begin() {
    startTime = System.nanoTime();
    token = server.login(logPrefix, logPrefix).right.get
    postCall()
  }

  def act() {
    while (true)
      receive {
        case Stop =>
          reply(logPrefix + "stopped")
          exit()
        case Run(times) =>
          reset()
          barrier.await()
          begin()
          for (i <- 1 to times) {
            val noticesIds = server.listNoticesIds(token).right.get
            postCall()
            for (id <- noticesIds) {
              currentId = id
              size += server.getNotice(token, id).fold(problem, _.toString.length())
              postCall()
            }
          }
          end();
          reply(logPrefix, duration, count, problemCount, addCount, updateCount, size)
      }
  }
}

object User {

  case class Run(times: Int)

  case object Stop

}