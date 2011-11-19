package edu.agh.lroza.common

import akka.dispatch.Future
import java.util.UUID
import akka.actor.{UntypedChannel, Actor}

case class Login(username: String, password: String)

case class Logout(token: UUID)

case class ListTopics(token: UUID)

class ServerActor(server: Server) extends Actor {
  protected def receive = {
    case Login(username, password) => {
      future(server.login(username, password), self.channel)
    }
    case Logout(token) => {
      future(server.logout(token), self.channel)
    }
    case ListTopics(token) => {
      future(server.listTopics(token), self.channel)
    }
  }

  private def future[T](code: => T, channel: UntypedChannel) = {
    Future {
      code
    } onComplete {
      _.value.get.fold(channel ! _, channel ! _)
    }
  }
}

object ServerActor {
  def apply(server: Server) = new ServerActor(server)
}