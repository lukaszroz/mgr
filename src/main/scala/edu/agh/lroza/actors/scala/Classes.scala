package edu.agh.lroza.actors.scala

import java.util.UUID
import edu.agh.lroza.common.Id
import akka.actor.{UntypedChannel, ActorRef}
import edu.agh.lroza.scalacommon.Problem

object Classes {

  case class Login(username: String, password: String);

  case class Logout(token: UUID);

  case class ListNoticesIds(override val token: UUID) extends NoticesMessage(token)

  case class AddNotice(override val token: UUID, title: String, message: String) extends NoticesMessage(token)

  case class GetNotice(override val token: UUID, override val id: Id) extends NoticeMessage(token, id)

  case class UpdateNotice(override val token: UUID, override val id: Id, title: String, message: String) extends NoticeMessage(token, id)

  case class DeleteNotice(override val token: UUID, override val id: Id) extends NoticeMessage(token, id)


  private[actors] class NoticesMessage(val token: UUID);

  private[actors] object NoticesMessage {
    def unapply(m: NoticesMessage): Some[UUID] = Some(m.token)
  }

  private[actors] class NoticeMessage(val token: UUID, val id: Id);

  private[actors] object NoticeMessage {
    def unapply(m: NoticeMessage): Some[(UUID, Id)] = Some(m.token, m.id)
  }

  private[actors] case class TitleReservedUpdateNotice(originalSender: UntypedChannel, title: String, message: String)

  private[actors] case class ReserveTitle(title: String, originalSender: UntypedChannel, returnMessage: AnyRef)

  private[actors] case class FreeTitle(title: String)

  private[actors] case class DeleteId(id: ActorId)


  private[actors] case class ActorId(actor: ActorRef) extends Id

  private[actors] val leftDeletedNotice = Left(Problem("Notice has been deleted"))

  private[actors] val problemNoSuchNotice = Problem("There is no such notice")
}
