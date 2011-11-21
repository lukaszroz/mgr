package edu.agh.lroza.synchronize

import java.util.UUID
import collection.mutable._
import edu.agh.lroza.common.{Topic, Problem, Server}

class SynchronizedServerScala extends Server {
  val loggedUsers = new HashMap[UUID, String] with SynchronizedMap[UUID, String]
  val topics = new HashMap[String, Topic] with SynchronizedMap[String, Topic]

  def login(username: String, password: String) = {
    if (username.equals(password)) {
      val token = UUID.randomUUID()
      loggedUsers += token -> username
      Some(token)
    } else {
      None
    }

  }

  def listTopics(token: UUID) = {
    checkIfLoggedAndDo(token) {
      Right(topics.keys)
    }
  }

  def logout(token: UUID) = {
    loggedUsers.remove(token) match {
      case Some(_: String) => true
      case None => false
    }
  }

  def addTopic(token: UUID, title: String, message: String) = {
    checkIfLoggedAndDo(token) {
      val topic = Topic(message)
      val stored = topics.getOrElseUpdate(title, topic)
      Either.cond(topic.equals(stored), stored, Problem("Topic already exists"))
    }
  }

  def getTopic(token: UUID, title: String) = {
    checkIfLoggedAndDo(token) {
      topics.get(title) match {
        case Some(topic) => Right(topic)
        case None => Left(Problem("There is no such topic '" + title + "'"))
      }
    }
  }

  def updateTopicTitle(token: UUID, oldTitle: String, newTitle: String) = {
    checkIfLoggedAndDo(token) {
      topics.synchronized {
        if (!topics.contains(oldTitle)) {
          Left(Problem("There is no such topic '" + oldTitle + "'"))
        } else if (topics.contains(newTitle)) {
          Left(Problem("Topic with title '" + newTitle + "' already exists"))
        } else {
          val topic = topics.remove(oldTitle).get
          topics += newTitle -> topic
          Right(topic)
        }
      }
    }
  }

  def deleteTopic(token: UUID, title: String) = {
    if (!loggedUsers.contains(token)) {
      false
    } else {
      topics.remove(title) match {
        case Some(_) => true
        case None => false
      }
    }
  }

  private def checkIfLoggedAndDo[T](token: UUID)(code: => Either[Problem, T]): Either[Problem, T] = {
    if (!loggedUsers.contains(token)) {
      Left(Problem("Please log in"))
    } else {
      code
    }
  }
}

object SynchronizedServerScala {
  def apply() = new SynchronizedServerScala

  def main(args: Array[String]) {
    //    run[SynchronizedServerScala]()
  }
}