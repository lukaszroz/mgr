package edu.agh.lroza.actors.scala

import edu.agh.lroza.actors.scala.Messages._
import edu.agh.lroza.scalacommon.Notice
import akka.actor.{ReceiveTimeout, ActorRef, Actor}

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