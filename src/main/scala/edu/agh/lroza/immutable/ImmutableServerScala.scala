package edu.agh.lroza.immutable

import java.util.UUID
import edu.agh.lroza.common._
import edu.agh.lroza.scalacommon._

class ImmutableServerScala extends NoticeBoardServerScala {
  @volatile var loggedUsers = Set[UUID]()
  val loggedUsersLock = new Object
  @volatile var notices = Map[Id, Notice]()
  val noticesLock = new Object

  case class TitleId(id: String) extends Id

  def login(username: String, password: String) = if (username.equals(password)) {
    val token = UUID.randomUUID()
    loggedUsersLock.synchronized {
      loggedUsers = loggedUsers + token
    }
    Right(token)
  } else {
    Left(Problem("Wrong password"))
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
      Some(Problem("Invalid token"))
    }
  }

  def listNoticesIds(token: UUID) = validateTokenEither(token) {
    Right(notices.keySet.toSet)
  }

  def addNotice(token: UUID, title: String, message: String) = validateTokenEither(token) {
    val notice = Notice(title, message)
    val id = TitleId(title)
    noticesLock.synchronized {
      if (!notices.contains(id)) {
        notices = notices + (id -> notice)
        Right(id)
      } else {
        Left(Problem("Topic with title '" + title + "' already exists"))
      }
    }
  }

  def getNotice(token: UUID, id: Id) = validateTokenEither(token) {
    notices.get(id) match {
      case Some(n) => Right(n)
      case None => Left(Problem("There is no such notice '" + id + "'"))
    }
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String) = validateTokenEither(token) {
    noticesLock.synchronized {
      if (!notices.contains(id)) {
        Left(Problem("There is no such notice '" + id + "'"))
      } else {
        val newId = TitleId(title)
        if (notices.contains(newId) && id != newId) {
          Left(Problem("Topic with title '" + title + "' already exists"))
        } else {
          notices = notices - id + (newId -> Notice(title, message))
          Right(newId)
        }
      }
    }
  }

  def deleteNotice(token: UUID, id: Id) = if (!loggedUsers.contains(token)) {
    Some(Problem("Invalid token"))
  } else {
    noticesLock.synchronized {
      if (notices.contains(id)) {
        notices = notices - id
        None
      } else {
        Some(Problem("There is no such notice '" + id + "'"))
      }
    }
  }

  private def validateTokenEither[T](token: UUID)(code: => Either[Problem, T]) = if (!loggedUsers.contains(token)) {
    Left(Problem("Invalid token"))
  } else {
    code
  }

}