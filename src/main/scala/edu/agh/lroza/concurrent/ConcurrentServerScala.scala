package edu.agh.lroza.concurrent

import edu.agh.lroza.common._
import collection.JavaConversions.asScalaConcurrentMap
import java.util.concurrent.ConcurrentHashMap
import java.lang.Object
import java.util.concurrent.atomic.AtomicLong
import edu.agh.lroza.scalacommon._
import com.eaio.uuid.UUID


class ConcurrentServerScala extends NoticeBoardServerScala {
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
    val token = new UUID()
    loggedUsers.put(token, o)
    Right(token)
  } else {
    Left(Problem("Wrong password"))
  }

  def logout(token: UUID) = if (loggedUsers.remove(token).isDefined) {
    None
  } else {
    Some(Problem("Invalid token"))
  }

  def listNoticesIds(token: UUID) = validateTokenEither(token) {
    Right(notices.keySet.toSet)
  }

  def addNotice(token: UUID, title: String, message: String) = validateTokenEither(token) {
    if (titleSet.putIfAbsent(title, false).isEmpty) {
      val id = LongId()
      notices.put(id, Notice(title, message))
      Right(id)
    } else {
      Left(Problem("Topic with title '" + title + "' already exists"))
    }
  }

  def getNotice(token: UUID, id: Id) = validateTokenEither(token) {
    notices.get(id).map(Right(_)).getOrElse(Left(Problem("There is no such notice '" + id + "'")))
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String) = validateTokenEither(token) {
    if (titleSet.putIfAbsent(title, true).isDefined &&
      !(notices.get(id).exists(_.title == title) && !titleSet.put(title, true).getOrElse(false))) {
      Left(Problem("Topic with title '" + title + "' already exists or was recently changed"))
    } else {
      notices.replace(id, Notice(title, message)) map {
        old => titleSet.put(title, false)
        if (title != old.title) {
          titleSet.remove(old.title)
        }
        Right(id)
      } getOrElse {
        titleSet.remove(title)
        Left(Problem("There is no such notice '" + id + "'"))
      }
    }
  }

  def deleteNotice(token: UUID, id: Id) = if (!loggedUsers.contains(token)) {
    Some(Problem("Invalid token"))
  } else {
    notices.remove(id) map {
      oldNotice => while (titleSet.contains(oldNotice.title) && !titleSet.remove(oldNotice.title, false)) {}
      None
    } getOrElse {
      Some(Problem("There is no such notice '" + id + "'"))
    }
  }

  private def validateTokenEither[T](token: UUID)(code: => Either[Problem, T]) = if (!loggedUsers.contains(token)) {
    Left(Problem("Invalid token"))
  } else {
    code
  }
}