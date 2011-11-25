package edu.agh.lroza.synchronize

import java.util.UUID
import collection.mutable._
import edu.agh.lroza.common._

case class TitleId(id: String) extends Id

class SynchronizedServerScala extends NoticeBoardServer {
  val loggedUsers = new HashMap[UUID, String] with SynchronizedMap[UUID, String]
  val notices = new HashMap[Id, Notice] with SynchronizedMap[Id, Notice]

  def login(username: String, password: String) = {
    if (username.equals(password)) {
      val token = UUID.randomUUID()
      loggedUsers += token -> username
      Right(token)
    } else {
      Left(ProblemS("Wrong password"))
    }

  }

  def logout(token: UUID) = {
    loggedUsers.remove(token) match {
      case Some(_: String) => None
      case None => Some(ProblemS("Invalid token"))
    }
  }

  def listNoticesIds(token: UUID) = {
    validateTokenEither(token) {
      Right(notices.keySet)
    }
  }

  def addNotice(token: UUID, title: String, message: String) = {
    validateTokenEither(token) {
      val notice = NoticeS(title, message)
      val stored = notices.getOrElseUpdate(TitleId(title), notice)
      Either.cond(notice.eq(stored), TitleId(title), ProblemS("Topic with title '" + title + "' already exists"))
    }
  }

  def getNotice(token: UUID, id: Id) = {
    validateTokenEither(token) {
      notices.get(id) match {
        case Some(topic) => Right(topic)
        case None => Left(ProblemS("There is no such topic '" + id + "'"))
      }
    }
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String) = {
    validateTokenEither(token) {
      notices.synchronized {
        if (!notices.contains(id)) {
          Left(ProblemS("There is no such topic '" + id + "'"))
        } else if (notices.contains(TitleId(title))) {
          Left(ProblemS("Topic with title '" + title + "' already exists"))
        } else {
          notices.remove(id).get
          notices += TitleId(title) -> NoticeS(title, message)
          Right(TitleId(title))
        }
      }
    }
  }

  def deleteNotice(token: UUID, id: Id) = {
    validateTokenOption(token) {
      notices.remove(id) match {
        case Some(_) => None
        case None => Some(ProblemS("There is no such topic '" + id + "'"))
      }
    }
  }

  private def validateTokenEither[T](token: UUID)(code: => Either[Problem, T]): Either[Problem, T] = {
    if (!loggedUsers.contains(token)) {
      Left(ProblemS("Please log in"))
    } else {
      code
    }
  }

  private def validateTokenOption(token: UUID)(code: => Option[Problem]): Option[Problem] = {
    if (!loggedUsers.contains(token)) {
      Some(ProblemS("Please log in"))
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