package edu.agh.lroza.common

import java.util.UUID
import scala.collection.Set

final case class ProblemS(message: String) extends Problem;

final case class NoticeS(title: String, message: String, author: String) extends Notice;

trait Problem {
  def message: String
}

trait Notice {
  def title: String

  def message: String

  def author: String
}

trait Id;

trait NoticeBoardServer {
  def login(username: String, password: String): Either[Problem, UUID]

  def logout(token: UUID): Option[Problem]

  def listTopics(token: UUID): Either[Problem, Set[Id]]

  def addTopic(token: UUID, title: String, message: String): Either[Problem, Id]

  def getTopic(token: UUID, id: Id): Either[Problem, Notice]

  def updateTopic(token: UUID, id: Id, newTitle: String, newMessage: String): Option[Problem]

  def deleteTopic(uuid: UUID, id: Id): Option[Problem]
}
