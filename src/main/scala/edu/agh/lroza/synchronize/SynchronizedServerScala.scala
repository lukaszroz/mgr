package edu.agh.lroza.synchronize

import java.util.UUID
import collection.mutable._
import edu.agh.lroza.common._
import edu.agh.lroza.scalacommon._

class SynchronizedServerScala extends NoticeBoardServerScala {
  val loggedUsers = new HashSet[UUID] with SynchronizedSet[UUID]
  val notices = new HashMap[Id, Notice] with SynchronizedMap[Id, Notice]

  case class TitleId(id: String) extends Id

  def login(username: String, password: String) = if (username.equals(password)) {
    val token = UUID.randomUUID()
    loggedUsers += token
    Right(token)
  } else {
    Left(Problem("Wrong password"))
  }

  def logout(token: UUID) = if (loggedUsers.remove(token)) {
    None
  } else {
    Some(Problem("Invalid token"))
  }

  def listNoticesIds(token: UUID) = validateToken(token) {
    Right(notices.keySet.toSet)
  }

  def addNotice(token: UUID, title: String, message: String) = validateToken(token) {
    val notice = Notice(title, message)
    val stored = notices.getOrElseUpdate(TitleId(title), notice)
    Either.cond(notice.equals(stored), TitleId(title), Problem("Topic with title '" + title + "' already exists"))
  }

  def getNotice(token: UUID, id: Id) = validateToken(token) {
    notices.get(id).map(Right(_)).getOrElse(Left(Problem("There is no such notice '" + id + "'")))
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String) = validateToken(token) {
    notices.synchronized {
      notices.get(id) map {
        notice => if (notice.title != title && notices.contains(TitleId(title))) {
          Left(Problem("Topic with title '" + title + "' already exists"))
        } else {
          if (notice.title != title) {
            notices.remove(id).get
          }
          notices += TitleId(title) -> Notice(title, message)
          Right(TitleId(title))
        }
      } getOrElse Left(Problem("There is no such notice '" + id + "'"))
    }
  }

  def deleteNotice(token: UUID, id: Id) = if (!loggedUsers.contains(token)) {
    Some(Problem("Invalid token"))
  } else {
    if (notices.remove(id).isDefined) {
      None
    } else {
      Some(Problem("There is no such notice '" + id + "'"))
    }
  }

  private def validateToken[T](token: UUID)(code: => Either[Problem, T]) = if (!loggedUsers.contains(token)) {
    Left(Problem("Invalid token"))
  } else {
    code
  }
}