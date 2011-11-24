package edu.agh.lroza.common

import akka.dispatch.Future
import java.util.UUID
import akka.actor.{UntypedChannel, Actor}

case class Login(username: String, password: String)

case class Logout(token: UUID)

case class ListTopics(token: UUID)

case class AddTopic(token: UUID, title: String, message: String)

case class GetTopic(token: UUID, title: String)

case class UpdateTopicTitle(token: UUID, oldTitle: String, newTitle: String)

case class DeleteTopic(token: UUID, title: String)

class ServerActor(server: NoticeBoardServer) extends Actor {
  protected def receive = {
    case Login(username, password) => {
      future(server.login(username, password), self.channel)
    }
    case Logout(token) => {
      future(server.logout(token), self.channel)
    }
    case ListTopics(token) => {
      future(server.listNoticesIds(token), self.channel)
    }
    case AddTopic(token, title, message) => {
      future(server.addNotice(token, title, message), self.channel)
    }
    case GetTopic(token, title) => {
      future(server.getNotice(token, title), self.channel)
    }
    case UpdateTopicTitle(token, oldTitle, newTitle) => {
      future(server.updateTopicTitle(token, oldTitle, newTitle), self.channel)
    }
    case DeleteTopic(token, title) => {
      future(server.deleteNotice(token, title), self.channel)
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
  def apply(server: NoticeBoardServer) = new ServerActor(server)
}