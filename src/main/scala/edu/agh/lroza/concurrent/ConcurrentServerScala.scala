package edu.agh.lroza.concurrent

import java.util.UUID
import edu.agh.lroza.common._
import collection.JavaConversions.asScalaConcurrentMap
import java.util.concurrent.ConcurrentHashMap
import java.lang.Object
import java.util.concurrent.atomic.AtomicLong


class ConcurrentServerScala extends NoticeBoardServer {
  val o = new Object
  val loggedUsers = asScalaConcurrentMap(new ConcurrentHashMap[UUID, Object]())
  val titleSet = asScalaConcurrentMap(new ConcurrentHashMap[String, Boolean]())
  val notices = asScalaConcurrentMap(new ConcurrentHashMap[Id, Notice]())

  case class LongId(id: Long) extends Id

  object LongId {
    private val generator = new AtomicLong();

    def apply(): Id = LongId(generator.getAndIncrement)
  }

  def login(username: String, password: String) = if (username.equals(password)) {
    val token = UUID.randomUUID()
    loggedUsers.put(token, o)
    Right(token)
  } else {
    Left(ProblemS("Wrong password"))
  }

  def logout(token: UUID) = if (loggedUsers.remove(token).isDefined) {
    None
  } else {
    Some(ProblemS("Invalid token"))
  }

  def listNoticesIds(token: UUID) = validateTokenEither(token) {
    Right(notices.keySet.toSet)
  }

  def addNotice(token: UUID, title: String, message: String) = validateTokenEither(token) {
    titleSet.putIfAbsent(title, false) match {
      case None =>
        val id = LongId()
        notices.put(id, NoticeS(title, message))
        Right(id)
      case Some(_) => Left(ProblemS("Topic with title '" + title + "' already exists"))
    }
  }

  def getNotice(token: UUID, id: Id) = validateTokenEither(token) {
    notices.get(id) match {
      case Some(n) => Right(n)
      case None => Left(ProblemS("There is no such notice '" + id + "'"))
    }
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String) = validateTokenEither(token) {
    if (titleSet.putIfAbsent(title, true).isDefined &&
      !(notices.get(id).exists(_.title == title) && !titleSet.put(title, true).getOrElse(false))) {
      Left(ProblemS("Topic with title '" + title + "' already exists or was recently changed"))
    } else {
      notices.replace(id, NoticeS(title, message)) match {
        case Some(old) =>
          titleSet.put(title, false)
          if (title != old.title) {
            titleSet.remove(old.title)
          }
          Right(id)
        case None =>
          titleSet.remove(title)
          Left(ProblemS("There is no such notice '" + id + "'"))
      }
    }
  }

  def deleteNotice(token: UUID, id: Id) = if (!loggedUsers.contains(token)) {
    Some(ProblemS("Invalid token"))
  } else {
    notices.remove(id) match {
      case Some(n) =>
        while (titleSet.contains(n.title) && !titleSet.remove(n.title, false)) {}
        None
      case None => Some(ProblemS("There is no such notice '" + id + "'"))
    }
  }

  private def validateTokenEither[T](token: UUID)(code: => Either[Problem, T]) = if (!loggedUsers.contains(token)) {
    Left(ProblemS("Invalid token"))
  } else {
    code
  }
}