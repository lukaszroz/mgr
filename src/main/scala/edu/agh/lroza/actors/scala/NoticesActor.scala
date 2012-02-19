package edu.agh.lroza.actors.scala

import akka.actor.Actor
import edu.agh.lroza.common.Id
import edu.agh.lroza.actors.scala.Classes._
import edu.agh.lroza.scalacommon.{Problem, Notice}


class NoticesActor extends Actor {
  var titles = Set[String]()
  var ids = Set[Id]()

  def listNoticesIds = Right(ids)

  def addNotice(title: String, message: String) = if (titles.contains(title)) {
    Left(Problem("Topic with title '" + title + "' already exists"))
  } else {
    titles = titles + title;
    val actorId = new ActorId(Actor.actorOf(new NoticeActor(self, Notice(title, message))).start())
    ids = ids + actorId
    Right(actorId)
  }

  protected def receive = {
    case ListNoticesIds(token) =>
      self reply listNoticesIds
    case AddNotice(token, title, message) =>
      self reply addNotice(title, message)
    case ReserveTitle(title, originalSender, message) =>
      if (titles.contains(title)) {
        originalSender ! Left(Problem("Topic with title '" + title + "' already exists"))
      } else {
        titles = titles + title
        if (!self.channel.tryTell(message)) {
          titles = titles - title
          originalSender ! leftDeletedNotice
        }
      }
    case FreeTitle(title) =>
      titles = titles - title
    case DeleteId(id) =>
      ids = ids - id
  }
}