package edu.agh.lroza.actors.scala

import java.util.UUID
import akka.actor.{UntypedChannel, ActorRef, Actor}
import edu.agh.lroza.common.Id
import edu.agh.lroza.actors.scala.LoginActor.ValidateToken
import edu.agh.lroza.actors.scala.NoticesActor._
import edu.agh.lroza.scalacommon.{Problem, Notice}


class NoticesActor(loginActor: ActorRef) extends Actor {
  var titles = Set[String]()
  var ids = Set[Id]()

  def listNoticesIds = Right(ids)

  def addNotice(title: String, message: String) = if (titles.contains(title)) {
    Left(Problem("Topic with title '" + title + "' already exists"))
  } else {
    titles = titles + title;
    val actorId = new ActorId(Actor.actorOf(new NoticeActor(self, loginActor, Notice(title, message))).start())
    ids = ids + actorId
    Right(actorId)
  }


  protected def receive = {
    case ListNoticesIds(token) =>
      loginActor ! ValidateToken(token, self.channel, false, ValidatedListNoticesIds(self.channel))
    case ValidatedListNoticesIds(originalSender) =>
      originalSender ! listNoticesIds
    case AddNotice(token, title, message) =>
      loginActor ! ValidateToken(token, self.channel, false, ValidatedAddNotice(self.channel, title, message))
    case ValidatedAddNotice(originalSender, title, message) =>
      originalSender ! addNotice(title, message)
    case ReserveTitle(title, originalSender, message) =>
      if (titles.contains(title)) {
        originalSender ! Left(Problem("Topic with title '" + title + "' already exists"))
      } else {
        titles = titles + title
        if (!self.channel.tryTell(message)) {
          titles = titles - title
          originalSender ! Left(Problem("Notice has been deleted"))
        }
      }
    case FreeTitle(title) =>
      titles = titles - title
    case DeleteId(id) =>
      ids = ids - id
  }
}

object NoticesActor {

  case class ListNoticesIds(token: UUID)

  case class AddNotice(token: UUID, title: String, message: String)

  private[actors] case class ActorId(actor: ActorRef) extends Id

  private[actors] case class ReserveTitle(title: String, originalSender: UntypedChannel, returnMessage: AnyRef)

  private[actors] case class FreeTitle(title: String)

  private[actors] case class DeleteId(id: ActorId)

  private case class ValidatedListNoticesIds(originalSender: UntypedChannel)

  private case class ValidatedAddNotice(originalSender: UntypedChannel, title: String, message: String)

}