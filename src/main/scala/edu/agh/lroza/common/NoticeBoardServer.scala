package edu.agh.lroza.common

import java.util.UUID
import scala.collection.Set

final case class ProblemS(message: String) extends Problem;

final case class NoticeS(title: String, message: String) extends Notice;

trait Problem {
  def message: String
}

trait Notice {
  def title: String

  def message: String
}

trait Id {
  def id: String
}

trait NoticeBoardServer {
  def login(username: String, password: String): Either[Problem, UUID]

  def logout(token: UUID): Option[Problem]

  def listNoticesIds(token: UUID): Either[Problem, Set[Id]]

  def addNotice(token: UUID, title: String, message: String): Either[Problem, Id]

  def getNotice(token: UUID, id: Id): Either[Problem, Notice]

  def updateNotice(token: UUID, id: Id, title: String, message: String): Option[Problem]

  def deleteNotice(uuid: UUID, id: Id): Option[Problem]
}
