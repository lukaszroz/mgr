package edu.agh.lroza.common

import java.util.UUID

case class Problem(message: String);

trait Server {
  def login(username: String, password: String): Option[UUID]

  def listTopics(token: UUID): Either[Problem, Iterable[String]]

  def logout(token: UUID): Boolean
}



