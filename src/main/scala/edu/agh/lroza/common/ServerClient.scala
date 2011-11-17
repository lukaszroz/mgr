package edu.agh.lroza.common

import akka.actor.ActorRef
import java.util.UUID

class ServerClient(server: ActorRef) extends Server {
  def login(username: String, password: String) = (server ? Login(username, password)).get match {
    case token: UUID => token
    case e: Exception => throw e
  }

  def listTopics() = (server ? ListTopics).get match {
    case list: Iterable[String] => list
    case e: Exception => throw e
  }

  def logout(token: UUID) = {
    (server ? Logout(token)).get match {
      case b: Boolean => b
      case e: Exception => throw e
    }
  }
}