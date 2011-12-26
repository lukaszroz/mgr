package edu.agh.lroza.actors.scala

import java.util.UUID
import edu.agh.lroza.scalacommon.Problem
import akka.actor.{ActorRef, Actor}
import edu.agh.lroza.common.Id
import edu.agh.lroza.actors.scala.LoginActor._

class LoginActor(noticesActor: ActorRef) extends Actor {
  var loggedUsers = Set[UUID]()

  def login(username: String, password: String): Either[Problem, UUID] = if (username == password) {
    val token = UUID.randomUUID()
    loggedUsers = loggedUsers + token
    Right(token)
  } else {
    Left(Problem("Wrong password"))
  }


  def logout(token: UUID): Option[Problem] = if (loggedUsers.contains(token)) {
    loggedUsers = loggedUsers - token
    None
  } else {
    Some(Problem("Invalid token"))
  }


  val notLogged = Left(Problem("Invalid token. Please log in."))

  def validateToken(token: UUID)(code: => Unit) {
    if (loggedUsers.contains(token)) {
      code
    } else {
      self reply notLogged
    }
  }

  protected def receive = {
    case message@NoticesMessage(token) => validateToken(token) {
      noticesActor.!(message)(self.getChannel)
    }
    case message@NoticeMessage(token, ActorId(noticeActor)) => validateToken(token) {
      if (!noticeActor.tryTell(message)(self.getChannel)) {
        self reply Left(Problem("Notice has been deleted"))
      }
    }
    case NoticeMessage(token, id) => validateToken(token) {
      self reply Left(Problem("There is no such notice '" + id + "'"))
    }
    case Login(username, password) => self reply login(username, password)
    case Logout(token) => self reply logout(token)
  }
}

object LoginActor {

  case class Login(username: String, password: String);

  case class Logout(token: UUID);

  private[actors] case class ActorId(actor: ActorRef) extends Id

  class NoticesMessage(val token: UUID);

  object NoticesMessage {
    def unapply(m: NoticesMessage): Some[UUID] = Some(m.token)
  }

  class NoticeMessage(val token: UUID, val id: Id);

  object NoticeMessage {
    def unapply(m: NoticeMessage): Some[(UUID, Id)] = Some(m.token, m.id)
  }

}