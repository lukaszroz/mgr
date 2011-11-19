package edu.agh.lroza.common

import akka.actor.ActorRef
import java.util.UUID

class ServerClient(server: ActorRef) extends Server {
  def login(username: String, password: String) = (server ? Login(username, password)).get match {
    case s: Some[UUID] => s
    case e: Exception => throw e
  }

  def listTopics(token: UUID) = (server ? ListTopics(token)).get match {
    case e: Either[Problem, Iterable[String]] => e
    case e: Exception => throw e
  }

  def logout(token: UUID) = (server ? Logout(token)).get match {
    case b: Boolean => b
    case e: Exception => throw e
  }

}