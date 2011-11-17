package edu.agh.lroza.common

import akka.actor.Actor
import akka.dispatch.Future
import java.util.UUID

case class Login(username: String, password: String)

case class Logout(token: UUID)

case object ListTopics

class ServerActor(server: Server) extends Actor {
  protected def receive = {
    case Login(username, password) => {
      val channel = self.channel
      Future {
        server.login(username, password)
      } onComplete {
        _.value.get.fold(channel ! _, channel ! _)
      }
    }
    case Logout(token) => {
      val channel = self.channel
      Future {
        server.logout(token)
      } onComplete {
        _.value.get.fold(channel ! _, channel ! _)
      }
    }
    case ListTopics => {
      val channel = self.channel
      Future {
        server.listTopics()
      } onComplete {
        _.value.get.fold(channel ! _, channel ! _)
      }
    }
  }
}

object ServerActor {
  def apply(server: Server) = new ServerActor(server)
}