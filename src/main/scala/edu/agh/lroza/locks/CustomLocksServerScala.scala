package edu.agh.lroza.locks

import java.util.UUID
import collection.mutable._
import edu.agh.lroza.common._
import actors.threadpool.locks.{Lock, ReentrantReadWriteLock}

class CustomLocksServerScala extends NoticeBoardServerScala {
  val loggedUsers = Set[UUID]()
  val notices = Map[Id, Notice]()
  val loggedUsersLock = new ReentrantReadWriteLock
  val noticesLock = new ReentrantReadWriteLock

  case class TitleId(id: String) extends Id

  private def lock[T](lock: Lock)(block: => T): T = {
    lock.lock()
    try {
      block
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
    Left(ProblemS("Wrong password"))
  }

  def logout(token: UUID) = lock(loggedUsersLock.writeLock()) {
    if (loggedUsers.remove(token)) {
      None
    } else {
      Some(ProblemS("Invalid token"))
    }
  }

  def listNoticesIds(token: UUID) = validateTokenEither(token) {
    lock(noticesLock.readLock()) {
      Right(notices.keySet.toSet)
    }
  }

  def addNotice(token: UUID, title: String, message: String) = validateTokenEither(token) {
    val notice = NoticeS(title, message)
    if (lock(noticesLock.readLock())(notices.contains(TitleId(title)))) {
      Left(ProblemS("Topic with title '" + title + "' already exists"))
    } else {
      val stored = lock(noticesLock.writeLock()) {
        notices.getOrElseUpdate(TitleId(title), notice)
      }
      Either.cond(notice.equals(stored), TitleId(title), ProblemS("Topic with title '" + title + "' already exists"))
    }
  }

  def getNotice(token: UUID, id: Id) = validateTokenEither(token) {
    lock(noticesLock.readLock()) {
      notices.get(id)
    } match {
      case Some(n) => Right(n)
      case None => Left(ProblemS("There is no such notice '" + id + "'"))
    }
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String) = validateTokenEither(token) {
    lock(noticesLock.writeLock()) {
      val noticeOption = notices.get(id)
      if (noticeOption.isEmpty) {
        Left(ProblemS("There is no such notice '" + id + "'"))
      } else if (noticeOption.get.title != title && notices.contains(TitleId(title))) {
        Left(ProblemS("Topic with title '" + title + "' already exists"))
      } else {
        if (noticeOption.get.title != title) {
          notices.remove(id).get
        }
        notices += TitleId(title) -> NoticeS(title, message)
        Right(TitleId(title))
      }
    }
  }

  def deleteNotice(token: UUID, id: Id) = if (!isValid(token)) {
    Some(ProblemS("Invalid token"))
  } else {
    lock(noticesLock.writeLock()) {
      notices.remove(id)
    } match {
      case Some(_) => None
      case None => Some(ProblemS("There is no such notice '" + id + "'"))
    }
  }

  def isValid(token: UUID) = lock(loggedUsersLock.readLock())(loggedUsers.contains(token))

  private def validateTokenEither[T](token: UUID)(code: => Either[Problem, T]) = if (!isValid(token)) {
    Left(ProblemS("Invalid token"))
  } else {
    code
  }
}