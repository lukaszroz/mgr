package edu.agh.lroza.common

import java.util.UUID
import scala.collection.Set

case class Problem(message: String);

trait Server {
  def login(username: String, password: String): Option[UUID]

  def logout(token: UUID): Boolean

  def listTopics(token: UUID): Either[Problem, Set[String]]

  def addTopic(token: UUID, title: String, message: String): Either[Problem, Topic]

  def getTopic(uuid: UUID, s: String): Either[Problem, Topic]

  def updateTopicTitle(token: UUID, oldTitle: String, newTitle: String): Either[Problem, Topic]

  def deleteTopic(uuid: UUID, title: String): Boolean
}
