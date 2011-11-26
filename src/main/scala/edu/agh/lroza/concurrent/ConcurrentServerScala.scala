package edu.agh.lroza.concurrent

import java.util.UUID
import edu.agh.lroza.common._
import collection.JavaConversions
import java.util.concurrent.ConcurrentHashMap
import java.lang.Object
import java.util.concurrent.atomic.AtomicLong

case class LongId(id: Long) extends Id

object LongId {
  private val generator = new AtomicLong();

  def apply(): Id = LongId(generator.getAndIncrement)
}

class ConcurrentServerScala extends NoticeBoardServer {
  val o = new Object
  val loggedUsers = JavaConversions.asScalaConcurrentMap(new ConcurrentHashMap[UUID, Object]())
  val titleSet = JavaConversions.asScalaConcurrentMap(new ConcurrentHashMap[String, Object]())
  val notices = JavaConversions.asScalaConcurrentMap(new ConcurrentHashMap[Id, Notice]())

  def login(username: String, password: String) = {
    if (username.equals(password)) {
      val token = UUID.randomUUID()
      loggedUsers.put(token, o)
      Right(token)
    } else {
      Left(ProblemS("Wrong password"))
    }

  }

  def logout(token: UUID) = {
    if (loggedUsers.remove(token).isDefined) {
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
      titleSet.putIfAbsent(title, o) match {
        case None =>
          val id = LongId()
          notices.put(id, NoticeS(title, message))
          Right(id)
        case Some(_) => Left(ProblemS("Topic with title '" + title + "' already exists"))
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
      if (titleSet.putIfAbsent(title, o).isDefined) {
        Left(ProblemS("Topic with title '" + title + "' already exists"))
      } else {
        notices.replace(id, NoticeS(title, message)) match {
          case Some(n) =>
            titleSet.remove(n.title)
            Right(id)
          case None =>
            titleSet.remove(title)
            Left(ProblemS("There is no such notice '" + id + "'"))
        }
      }
    }
  }

  def deleteNotice(token: UUID, id: Id) = {
    validateTokenOption(token) {
      notices.remove(id) match {
        case Some(n) =>
          titleSet.remove(n.title)
          None
        case None => Some(ProblemS("There is no such notice '" + id + "'"))
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

object ConcurrentServerScala {
  def apply() = new ConcurrentServerScala
}

