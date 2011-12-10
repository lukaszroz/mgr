package edu.agh.lroza.common

import java.util.UUID
import actors.Actor
import edu.agh.lroza.common.User.{Stop, Run}
import java.util.concurrent.{CyclicBarrier, TimeUnit}


class User(server: NoticeBoardServer, name: String, barrier: CyclicBarrier) extends Actor {
  var token = UUID.randomUUID()
  val logPrefix = "[" + name + "]"
  var size, startTime, duration, count, problemCount = 0L

  def reset() {
    duration = 0L
    count = 0L
    problemCount = 0L
  }

  def postCall() {
    count += 1
    if (count % 100 == 50) {
      server.logout(token)
      token = server.login(name, name).right.get
      count += 2
    }
  }

  def end() {
    server.logout(token)
    postCall()
    duration += TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime)
  }

  def begin() {
    startTime = System.nanoTime();
    token = server.login(name, name).right.get
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
              size += (server.getNotice(token, id) match {
                case Right(notice) => notice.toString.length()
                case Left(problem) => problemCount += 1; -1
              })
              postCall()
            }
          }
          end();
          reply(logPrefix, duration, count, problemCount)
      }
  }
}

object User {

  case class Run(times: Int)

  case object Stop

}