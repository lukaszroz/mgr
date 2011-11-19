package edu.agh.lroza.synchronize

import java.util.UUID
import collection.mutable._
import edu.agh.lroza.common.{Problem, Server}

class SynchronizedServerScala extends Server {
  val loggedUsers = new HashMap[UUID, String] with SynchronizedMap[UUID, String]

  def login(username: String, password: String) = {
    if (username.equals(password)) {
      val token = UUID.randomUUID()
      loggedUsers += token -> username
      Some(token)
    } else {
      None
    }

  }

  def listTopics(token: UUID) = {
    Either.cond(loggedUsers.contains(token), Iterable[String]("a", "b", "c"), Problem("Please log in"))
  }

  def logout(token: UUID) = {
    loggedUsers.remove(token) match {
      case Some(_: String) => true
      case None => false
    }
  }
}

object SynchronizedServerScala {
  def apply() = new SynchronizedServerScala

  def main(args: Array[String]) {
    //    run[SynchronizedServerScala]()
  }
}