package edu.agh.lroza.actors

import java.util.UUID
import akka.actor.{UntypedChannel, ActorRef, Actor}
import edu.agh.lroza.common.{NoticeS, Id, ProblemS}
import akka.event.EventHandler
import edu.agh.lroza.actors.NoticesActorS._
import edu.agh.lroza.actors.LoginActorS.ValidateToken


class NoticesActorS(loginActor: ActorRef) extends Actor {
  var titles = Set[String]()
  var ids = Set[Id]()

  def listNoticesIds = Right(ids)

  def addNotice(title: String, message: String) = {
    if (titles.contains(title)) {
      Left(ProblemS("Topic with title '" + title + "' already exists"))
    } else {
      titles = titles + title;
      val actorId = new ActorId(Actor.actorOf(new NoticeActorS(self, loginActor, NoticeS(title, message))).start())
      ids = ids + actorId
      Right(actorId)
    }
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
      EventHandler.debug(this, "titles=" + titles + "; title=" + title)
      if (titles.contains(title)) {
        originalSender ! Left(ProblemS("Topic with title '" + title + "' already exists"))
      } else {
        titles = titles + title
        if (!self.channel.tryTell(message)) {
          titles = titles - title
          originalSender ! Left(ProblemS("Notice has been deleted"))
        }
      }
    case FreeTitle(title) =>
      titles = titles - title
    case DeleteId(id) =>
      ids = ids - id
  }
}

object NoticesActorS {

  case class ListNoticesIds(token: UUID)

  case class AddNotice(token: UUID, title: String, message: String)

  private[actors] case class ActorId(actor: ActorRef) extends Id

  private[actors] case class ReserveTitle(title: String, originalSender: UntypedChannel, returnMessage: AnyRef)

  private[actors] case class FreeTitle(title: String)

  private[actors] case class DeleteId(id: ActorId)

  private case class ValidatedListNoticesIds(originalSender: UntypedChannel)

  private case class ValidatedAddNotice(originalSender: UntypedChannel, title: String, message: String)

}