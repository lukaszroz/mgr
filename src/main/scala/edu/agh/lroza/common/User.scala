package edu.agh.lroza.common

import java.util.UUID
import actors.Actor
import edu.agh.lroza.common.User.{Stop, Logout, Login, ReadAll}
import java.util.concurrent.TimeUnit


class User(server: NoticeBoardServer, name: String) extends Actor {
  var token = UUID.randomUUID()
  val logPrefix = "[" + name + "] "
  var size = 0L

  def act() {
    while (true)
      receive {
        case Login =>
          println("--------------------------------------")
          println(logPrefix + "Login")
          token = server.login(name, name).right.get
        case Logout =>
          println("--------------------------------------")
          println(logPrefix + "Logout")
          server.logout(token)
        case Stop =>
          println("--------------------------------------")
          println(logPrefix + "Stop")
          reply("Done")
          exit()
        case ReadAll(times) =>
          println("--------------------------------------")
          println(logPrefix + "ReadAll(" + times + ")")
          val start = System.nanoTime();
          for (i <- 1 to times) {
            val noticesIds = server.listNoticesIds(token).right.get
            for (id <- noticesIds) {
              size += server.getNotice(token, id).right.get.toString.length()
              //              println(logPrefix + "got message: " + server.getNotice(token, id).right.get)
            }
          }
          val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
          reply(name, duration)
          println(logPrefix + "duration: " + duration + "ms")
      }
  }
}

object User {

  case class ReadAll(times: Int)

  case object Login

  case object Logout

  case object Stop

}