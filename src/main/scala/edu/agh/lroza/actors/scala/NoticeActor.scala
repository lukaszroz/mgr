package edu.agh.lroza.actors.scala

import java.util.UUID
import edu.agh.lroza.actors.scala.NoticeActor._
import edu.agh.lroza.actors.scala.NoticesActor.{DeleteId, ReserveTitle, FreeTitle}
import edu.agh.lroza.scalacommon.{Problem, Notice}
import akka.actor.{ReceiveTimeout, UntypedChannel, ActorRef, Actor}
import edu.agh.lroza.common.Id
import edu.agh.lroza.actors.scala.LoginActor.{ActorId, NoticeMessage}

class NoticeActor(noticesActor: ActorRef, var notice: Notice) extends Actor {

  def updateNotice(title: String, message: String) = {
    val oldTitle = notice.title
    notice = Notice(title, message)
    noticesActor ! FreeTitle(oldTitle)
    Right(ActorId(self))
  }

  protected def receive = {
    case GetNotice(_, _) =>
      self reply Right(notice)
    case UpdateNotice(_, _, title, message) =>
      if (title == notice.title) {
        notice = Notice(title, message)
        self reply Right(ActorId(self))
      } else {
        val channel = self.getChannel
        noticesActor ! ReserveTitle(title, channel, ValidatedUpdateNotice(channel, title, message))
      }
    case ValidatedUpdateNotice(originalSender, title, message) =>
      originalSender ! updateNotice(title, message)
    case DeleteNotice(_, _) =>
      noticesActor ! DeleteId(ActorId(self))
      noticesActor ! FreeTitle(notice.title)
      self reply None
      self.receiveTimeout = Some(50L)
      become(deletedReceive)
  }

  val deletedReceive: Receive = {
    case DeleteNotice(_, _) => self.reply(Some(problemNoSuchNotice))
    case ValidatedUpdateNotice(originalSender, title, message) =>
      noticesActor ! FreeTitle(title)
      originalSender ! leftDeletedNotice
    case ReceiveTimeout => self.stop()
    case x => self.reply(Left(problemNoSuchNotice))
  }
}

object NoticeActor {

  case class GetNotice(override val token: UUID, override val id: Id) extends NoticeMessage(token, id)

  case class UpdateNotice(override val token: UUID, override val id: Id, title: String, message: String) extends NoticeMessage(token, id)

  case class DeleteNotice(override val token: UUID, override val id: Id) extends NoticeMessage(token, id)

  private case class ValidatedUpdateNotice(originalSender: UntypedChannel, title: String, message: String)

  val leftDeletedNotice = Left(Problem("Notice has been deleted"))

  val problemNoSuchNotice = Problem("There is no such notice")
}