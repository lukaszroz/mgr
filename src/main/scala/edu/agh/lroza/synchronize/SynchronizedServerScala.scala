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

  def listNoticesIds(token: UUID) = validateTokenEither(token) {
    Right(notices.keySet.toSet)
  }

  def addNotice(token: UUID, title: String, message: String) = validateTokenEither(token) {
    val notice = Notice(title, message)
    val stored = notices.getOrElseUpdate(TitleId(title), notice)
    Either.cond(notice.equals(stored), TitleId(title), Problem("Topic with title '" + title + "' already exists"))
  }

  def getNotice(token: UUID, id: Id) = validateTokenEither(token) {
    notices.get(id) match {
      case Some(n) => Right(n)
      case None => Left(Problem("There is no such notice '" + id + "'"))
    }
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String) = validateTokenEither(token) {
    notices.synchronized {
      val noticeOption = notices.get(id)
      if (noticeOption.isEmpty) {
        Left(Problem("There is no such notice '" + id + "'"))
      } else if (noticeOption.get.title != title && notices.contains(TitleId(title))) {
        Left(Problem("Topic with title '" + title + "' already exists"))
      } else {
        if (noticeOption.get.title != title) {
          notices.remove(id).get
        }
        notices += TitleId(title) -> Notice(title, message)
        Right(TitleId(title))
      }
    }
  }

  def deleteNotice(token: UUID, id: Id) = if (!loggedUsers.contains(token)) {
    Some(Problem("Invalid token"))
  } else {
    notices.remove(id) match {
      case Some(_) => None
      case None => Some(Problem("There is no such notice '" + id + "'"))
    }
  }

  private def validateTokenEither[T](token: UUID)(code: => Either[Problem, T]) = if (!loggedUsers.contains(token)) {
    Left(Problem("Invalid token"))
  } else {
    code
  }
}