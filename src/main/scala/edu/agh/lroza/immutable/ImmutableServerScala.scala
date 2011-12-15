package edu.agh.lroza.immutable

import java.util.UUID
import edu.agh.lroza.common._

class ImmutableServerScala extends NoticeBoardServer {
  @volatile
  var loggedUsers = Set[UUID]()
  val loggedUsersLock = new Object
  @volatile
  var notices = Map[Id, Notice]()
  val noticesLock = new Object

  case class TitleId(id: String) extends Id

  def login(username: String, password: String) = {
    if (username.equals(password)) {
      val token = UUID.randomUUID()
      loggedUsersLock.synchronized {
        loggedUsers = loggedUsers + token
      }
      Right(token)
    } else {
      Left(ProblemS("Wrong password"))
    }

  }

  def logout(token: UUID) = {
    val changed = loggedUsersLock.synchronized {
      val old = loggedUsers
      loggedUsers = loggedUsers - token
      old ne loggedUsers
    }
    if (changed) {
      None
    } else {
      Some(ProblemS("Invalid token"))
    }
  }

  def listNoticesIds(token: UUID) = {
    validateTokenEither(token) {
      Right(notices.keySet.toSet)
    }
  }

  def addNotice(token: UUID, title: String, message: String) = {
    validateTokenEither(token) {
      val notice = NoticeS(title, message)
      val id = TitleId(title)
      noticesLock.synchronized {
        if (!notices.contains(id)) {
          notices = notices + (id -> notice)
          Right(id)
        } else {
          Left(ProblemS("Topic with title '" + title + "' already exists"))
        }
      }
    }
  }

  def getNotice(token: UUID, id: Id) = {
    validateTokenEither(token) {
      notices.get(id) match {
        case Some(n) => Right(n)
        case None => Left(ProblemS("There is no such notice '" + id + "'"))
      }
    }
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String) = {
    validateTokenEither(token) {
      noticesLock.synchronized {
        if (!notices.contains(id)) {
          Left(ProblemS("There is no such notice '" + id + "'"))
        } else {
          val newId = TitleId(title)
          if (notices.contains(newId) && id != newId) {
            Left(ProblemS("Topic with title '" + title + "' already exists"))
          } else {
            notices = notices - id + (newId -> NoticeS(title, message))
            Right(newId)
          }
        }
      }
    }
  }

  def deleteNotice(token: UUID, id: Id) = {
    validateTokenOption(token) {
      noticesLock.synchronized {
        if (notices.contains(id)) {
          notices = notices - id
          None
        } else {
          Some(ProblemS("There is no such notice '" + id + "'"))
        }
      }
    }
  }

  private def validateTokenEither[T](token: UUID)(code: => Either[Problem, T]): Either[Problem, T] = {
    if (!loggedUsers.contains(token)) {
      Left(ProblemS("Invalid token"))
    } else {
      code
    }
  }

  private def validateTokenOption(token: UUID)(code: => Option[Problem]): Option[Problem] = {
    if (!loggedUsers.contains(token)) {
      Some(ProblemS("Invalid token"))
    } else {
      code
    }
  }
}