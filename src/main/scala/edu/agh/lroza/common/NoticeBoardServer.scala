package edu.agh.lroza.common

import java.util.UUID
import scala.collection.Set

trait Problem {
  def message: String
}

trait Notice {
  def title: String

  def message: String
}

trait Id

trait NoticeBoardServer {
  def login(username: String, password: String): Either[Problem, UUID]

  def logout(token: UUID): Option[Problem]

  def listNoticesIds(token: UUID): Either[Problem, Set[Id]]

  def addNotice(token: UUID, title: String, message: String): Either[Problem, Id]

  def getNotice(token: UUID, id: Id): Either[Problem, Notice]

  def updateNotice(token: UUID, id: Id, title: String, message: String): Either[Problem, Id]

  def deleteNotice(token: UUID, id: Id): Option[Problem]
}