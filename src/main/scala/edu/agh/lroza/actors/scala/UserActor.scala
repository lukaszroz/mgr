package edu.agh.lroza.actors.scala

import edu.agh.lroza.scalacommon.Problem
import edu.agh.lroza.common.Id
import edu.agh.lroza.actors.scala.UserActor._
import com.eaio.uuid.UUID
import edu.agh.lroza.actors.scala.NoticeActor.DeleteNotice
import akka.actor.{ReceiveTimeout, ActorRef, Actor}

class UserActor(noticesActor: ActorRef) extends Actor {
  //  var loggedUsers = Set[UUID]()

  //  def login(username: String, password: String): Either[Problem, UUID] = if (username == password) {
  //    val token = UUID.randomUUID()
  //    loggedUsers = loggedUsers + token
  //    Right(token)
  //  } else {
  //    Left(Problem("Wrong password"))
  //  }
  //
  //
  //  def logout(token: UUID): Option[Problem] = if (loggedUsers.contains(token)) {
  //    loggedUsers = loggedUsers - token
  //    None
  //  } else {
  //    Some(Problem("Invalid token"))
  //  }


  val notLogged = Left(Problem("Invalid token. Please log in."))

  //  def validateToken(token: UUID)(code: => Unit) {
  //        if (loggedUsers.contains(token)) {
  //          code
  //        } else {
  //          self reply notLogged
  //        }
  //  }

  protected def receive = {
    case message@NoticesMessage(token) =>
      noticesActor.!(message)(self.getChannel)

    case message@NoticeMessage(token, ActorId(noticeActor)) =>
      if (!noticeActor.tryTell(message)(self.getChannel)) {
        self reply Left(Problem("Notice has been deleted"))
      }

    case NoticeMessage(token, id) =>
      self reply Left(Problem("There is no such notice '" + id + "'"))

    //    case Login(username, password) => self reply login(username, password)
    case Logout =>
      self.receiveTimeout = Some(50L)
      become(logoutReceive)
      self reply None
  }

  val logoutReceive: Receive = {
    case ReceiveTimeout => self.stop()
    case Logout | DeleteNotice => self.reply(Some(Problem("Invalid Token")))
    case _ => self.reply(Left(Problem("Invalid Token")))
  }
}

object UserActor {

  //  case class Login(username: String, password: String);
  //
  case object Logout;

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