package edu.agh.lroza.common

import akka.actor.ActorRef
import java.util.UUID

class ServerClient(server: ActorRef) extends Server {
  def login(username: String, password: String) = (server ? Login(username, password)).as[Some[UUID]] match {
    case Some(answer) => answer
    case None => None
  }

  def listTopics(token: UUID) = (server ? ListTopics(token)).as[Either[Problem, Iterable[String]]] match {
    case Some(answer) => answer
    case None => Left(Problem("Timeout occured"))
  }

  def logout(token: UUID) = (server ? Logout(token)).as[Boolean] match {
    case Some(b) => b
    case None => false
  }

  def addTopic(token: UUID, title: String, message: String) = (server ? AddTopic(token, title, message)).as[Either[Problem, Topic]] match {
    case Some(answer) => answer
    case None => Left(Problem("Timeout occured"))
  }

  def getTopic(token: UUID, title: String) = (server ? GetTopic(token, title)).as[Either[Problem, Topic]] match {
    case Some(answer) => answer
    case None => Left(Problem("Timeout occured"))
  }

  def updateTopic(token: UUID, oldTitle: String, newTitle: String) = (server ? UpdateTopic(token, oldTitle, newTitle))
    .as[Either[Problem, Topic]] match {
    case Some(answer) => answer
    case None => Left(Problem("Timeout occured"))
  }

  def deleteTopic(token: UUID, title: String) = (server ? DeleteTopic(token, title)).as[Boolean] match {
    case Some(b) => b
    case None => false
  }
}