package edu.agh.lroza.locks

import java.util.UUID
import collection.mutable._
import edu.agh.lroza.common._
import actors.threadpool.locks.{Lock, ReentrantReadWriteLock}

case class TitleId(id: String) extends Id

class CustomLocksServerScala extends NoticeBoardServer {
  val loggedUsers = new HashSet[UUID]
  val notices = new HashMap[Id, Notice]
  val loggedUsersLock = new ReentrantReadWriteLock
  val noticesLock = new ReentrantReadWriteLock

  private def lock[T](lock: Lock)(block: => T): T = {
    lock.lock()
    try {
      block
    } finally {
      lock.unlock()
    }
  }

  def login(username: String, password: String) = {
    if (username.equals(password)) {
      val token = UUID.randomUUID()
      lock(loggedUsersLock.writeLock()) {
        loggedUsers += token
      }
      Right(token)
    } else {
      Left(ProblemS("Wrong password"))
    }

  }

  def logout(token: UUID) = {
    lock(loggedUsersLock.writeLock()) {
      if (loggedUsers.remove(token)) {
        None
      } else {
        Some(ProblemS("Invalid token"))
      }
    }
  }

  def listNoticesIds(token: UUID) = {
    validateTokenEither(token) {
      lock(noticesLock.readLock()) {
        Right(notices.keySet)
      }
    }
  }

  def addNotice(token: UUID, title: String, message: String) = {
    validateTokenEither(token) {
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
  }

  def getNotice(token: UUID, id: Id) = {
    validateTokenEither(token) {
      lock(noticesLock.readLock()) {
        notices.get(id)
      } match {
        case Some(n) => Right(n)
        case None => Left(ProblemS("There is no such notice '" + id + "'"))
      }
    }
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String) = {
    validateTokenEither(token) {
      lock(noticesLock.writeLock()) {
        if (!notices.contains(id)) {
          Left(ProblemS("There is no such notice '" + id + "'"))
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
      lock(noticesLock.writeLock()) {
        notices.remove(id)
      } match {
        case Some(_) => None
        case None => Some(ProblemS("There is no such notice '" + id + "'"))
      }
    }
  }

  private def validateTokenEither[T](token: UUID)(code: => Either[Problem, T]): Either[Problem, T] = {
    val isValid = lock(loggedUsersLock.readLock())(loggedUsers.contains(token))
    if (!isValid) {
      Left(ProblemS("Invalid token"))
    } else {
      code
    }
  }

  private def validateTokenOption(token: UUID)(code: => Option[Problem]): Option[Problem] = {
    val isValid = lock(loggedUsersLock.readLock())(loggedUsers.contains(token))
    if (!isValid) {
      Some(ProblemS("Invalid token"))
    } else {
      code
    }
  }
}

object CustomLocksServerScala {
  def apply() = new CustomLocksServerScala
}