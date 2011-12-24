package edu.agh.lroza.simulation

import java.util.UUID
import actors.Actor
import edu.agh.lroza.simulation.ScalaUser.{Stop, Run}
import java.util.concurrent.{CyclicBarrier, TimeUnit}
import edu.agh.lroza.common.Id
import edu.agh.lroza.scalacommon.{Problem, NoticeBoardServerScala}


class ScalaUser(server: NoticeBoardServerScala, number: Int, barrier: CyclicBarrier, writeEvery: Int) extends Actor {
  var token = UUID.randomUUID()
  val logPrefix = "[client%2d]".format(number)
  var startTime, duration, count, problemCount = 0L
  var occupySum = 0D
  var currentId: Id = null
  val shift = if (writeEvery == 0) 0 else number * 11 % writeEvery

  var nextWriteAction: () => Unit = null

  def problem(p: Problem) = {
    problemCount += 1
    -1
  }

  val addNotice: () => Unit = () => {
    addCount += 1
    val length = server.addNotice(token, getTitle(addCount), "message" + logPrefix).fold(problem, _.toString.length())
    postCall(length)
    nextWriteAction = updateNotice
  }

  val updateNotice: () => Unit = () => {
    updateCount += 1
    val length = server.updateNotice(token, currentId, getTitle(updateCount), "message" + logPrefix)
      .fold(problem, _.toString.length())
    postCall(length)
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
    val length = server.deleteNotice(token, currentId).map(problem(_)).getOrElse(1)
    postCall(length)
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

  def postCall(length: Int) {
    occupyProcessor(length)
    count += 1
    if (count % 100 == 50) {
      server.logout(token).foreach(_ => problemCount += 1)
      token = server.login(logPrefix, logPrefix).right.get
      postCall(1)
      postCall(token.toString.length())
    }

    if (writeEvery > 1 && count % writeEvery == shift) {
      nextWriteAction()
    }
  }

  def end() {
    server.logout(token).foreach(_ => problemCount += 1)
    postCall(1)
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
    postCall(token.toString.length())
  }

  def occupyProcessor(i: Int) {
    //    import scala.math._
    import java.lang.Math._
    var c = 0
    while (c < i) {
      occupySum += sqrt(pow(11, 0.11)).ceil
      //      occypySum += sqrt(11).ceil
      c += 12
    }
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
            postCall(noticesIds.toString().length())
            for (id <- noticesIds) {
              currentId = id
              val length = server.getNotice(token, id).fold(problem, _.toString.length())
              postCall(length)
            }
          }
          end();
          reply(logPrefix, duration, count, problemCount, addCount, updateCount)
      }
  }
}

object ScalaUser {

  case class Run(times: Int)

  case object Stop

}