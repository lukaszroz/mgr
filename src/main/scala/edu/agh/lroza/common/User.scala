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

  val addNotice: () => Unit = () => {
    addCount += 1
    server.addNotice(token, getTitle(addCount), "message" + logPrefix)
    postCall()
    nextWriteAction = updateNotice
  }

  val updateNotice: () => Unit = () => {
    updateCount += 1
    server.updateNotice(token, currentId, getTitle(updateCount), "message" + logPrefix)
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
    server.deleteNotice(token, currentId)
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
      server.logout(token)
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
    server.logout(token)
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
              size += (server.getNotice(token, id) match {
                case Right(notice) => notice.toString.length()
                case Left(problem) => problemCount += 1; -1
              })
              postCall()
            }
          }
          end();
          reply(logPrefix, duration, count, problemCount, addCount, updateCount)
      }
  }
}

object User {

  case class Run(times: Int)

  case object Stop

}