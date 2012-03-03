package edu.agh.lroza.locks

import java.util.UUID
import collection.mutable._
import edu.agh.lroza.common._
import actors.threadpool.locks.{Lock, ReentrantReadWriteLock}
import edu.agh.lroza.scalacommon._

class CustomLocksServerScala extends NoticeBoardServerScala {
  val loggedUsers = Set[UUID]()
  val notices = Map[Id, Notice]()
  val loggedUsersLock = new ReentrantReadWriteLock
  val noticesLock = new ReentrantReadWriteLock

  private def lock[T](lock: Lock)(code: => T): T = {
    lock.lock()
    try {
      code
    } finally {
      lock.unlock()
    }
  }

  def login(username: String, password: String) = if (username.equals(password)) {
    val token = UUID.randomUUID()
    lock(loggedUsersLock.writeLock()) {
      loggedUsers += token
    }
    Right(token)
  } else {
    Left(Problem("Wrong password"))
  }

  def logout(token: UUID) = lock(loggedUsersLock.writeLock()) {
    if (loggedUsers.remove(token)) {
      None
    } else {
      Some(Problem("Invalid token"))
    }
  }

  def listNoticesIds(token: UUID) = validateTokenEither(token) {
    lock(noticesLock.readLock()) {
      Right(notices.keySet.toSet)
    }
  }

  def addNotice(token: UUID, title: String, message: String) = validateTokenEither(token) {
    val notice = Notice(title, message)
    if (lock(noticesLock.readLock())(notices.contains(TitleId(title)))) {
      Left(Problem("Topic with title '" + title + "' already exists"))
    } else {
      val stored = lock(noticesLock.writeLock()) {
        notices.getOrElseUpdate(TitleId(title), notice)
      }
      Either.cond(notice.equals(stored), TitleId(title), Problem("Topic with title '" + title + "' already exists"))
    }
  }

  def getNotice(token: UUID, id: Id) = validateTokenEither(token) {
    lock(noticesLock.readLock()) {
      notices.get(id)
    }.map(Right(_)).getOrElse(Left(Problem("There is no such notice '" + id + "'")))
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String) = validateTokenEither(token) {
    lock(noticesLock.writeLock()) {
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

  def deleteNotice(token: UUID, id: Id) = if (!isValid(token)) {
    Some(Problem("Invalid token"))
  } else {
    if (lock(noticesLock.writeLock())(notices.remove(id).isDefined)) {
      None
    } else {
      Some(Problem("There is no such notice '" + id + "'"))
    }
  }

  def isValid(token: UUID) = lock(loggedUsersLock.readLock())(loggedUsers.contains(token))

  private def validateTokenEither[T](token: UUID)(code: => Either[Problem, T]) = if (!isValid(token)) {
    Left(Problem("Invalid token"))
  } else {
    code
  }
}