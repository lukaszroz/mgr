package edu.agh.lroza.simulation

import java.util.UUID
import actors.Actor
import java.util.concurrent.{CyclicBarrier, TimeUnit}
import edu.agh.lroza.common.Id
import edu.agh.lroza.simulation.User.Run
import edu.agh.lroza.javacommon.{ProblemException, NoticeBoardServerJava}


class JavaUser(server: NoticeBoardServerJava, number: Int, barrier: CyclicBarrier, writeEvery: Int) extends Actor {
  var token = UUID.randomUUID()
  val logPrefix = "[client%2d]".format(number)
  var startTime, duration, count, problemCount = 0L
  var occupySum = 0D
  var currentId: Id = null
  val shift = if (writeEvery == 0) 0 else number * 11 % writeEvery

  var nextWriteAction: () => Unit = null

  def problem(p: ProblemException) = {
    problemCount += 1
    -1
  }

  val addNotice: () => Unit = () => {
    addCount += 1
    val length = try {
      server.addNotice(token, getTitle(addCount), "message" + logPrefix).toString.length()
    } catch {
      case e: ProblemException => problem(e)
    }
    postCall(length)
    nextWriteAction = updateNotice
  }

  val updateNotice: () => Unit = () => {
    updateCount += 1
    val length = try {
      server.updateNotice(token, currentId, getTitle(updateCount), "message" + logPrefix).toString.length()
    } catch {
      case e: ProblemException => problem(e)
    }
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
    val length = try {
      server.deleteNotice(token, currentId)
      1
    } catch {
      case e: ProblemException => problem(e)
    }
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
      try {
        server.logout(token)
      } catch {
        case e: ProblemException => problemCount += 1
      }
      token = server.login(logPrefix, logPrefix)
      postCall(1)
      postCall(token.toString.length())
    }

    if (writeEvery > 1 && count % writeEvery == shift) {
      nextWriteAction()
    }
  }

  def end() {
    try {
      server.logout(token)
    } catch {
      case e: ProblemException => problemCount += 1
    }
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
    token = server.login(logPrefix, logPrefix)
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
        case User.Stop =>
          reply(logPrefix + "stopped")
          exit()
        case Run(times) =>
          reset()
          barrier.await()
          begin()
          for (i <- 1 to times) {
            val noticesIds = server.listNoticesIds(token)
            postCall(noticesIds.toString.length())
            val iterator = noticesIds.iterator()
            while (iterator.hasNext) {
              val id = iterator.next
              currentId = id
              val length = try {
                server.getNotice(token, id).toString.length()
              } catch {
                case e: ProblemException => problem(e)
              }
              postCall(length)
            }
          }
          end();
          reply(logPrefix, duration, count, problemCount, addCount, updateCount)
      }
  }
}