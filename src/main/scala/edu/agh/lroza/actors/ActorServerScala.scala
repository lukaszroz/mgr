package edu.agh.lroza.actors

import _root_.java.util.UUID
import edu.agh.lroza.common._
import akka.actor.Actor
import edu.agh.lroza.actors.scala.Messages._
import scala.{NoticesActor, LoginActor}
import edu.agh.lroza.scalacommon.{Notice, Problem, NoticeBoardServerScala}
import akka.dispatch.Future

class ActorServerScala extends NoticeBoardServerScala {
  val noticesActor = Actor.actorOf[NoticesActor].start()
  val loginActor = Actor.actorOf(new LoginActor(noticesActor)).start()

  val leftTimeout = Left(Problem("Timeout occurred"))
  val someTimeout = Some(Problem("Timeout occurred"))

  def login(username: String, password: String) =
    (loginActor ? Login(username, password)).as[Either[Problem, UUID]].get

  def logout(token: UUID) = (loginActor ? Logout(token)).as[Option[Problem]].get

  def listNoticesIds(token: UUID): Either[Problem, Set[Id]] =
    (loginActor ? ListNoticesIds(token)).as[Either[Problem, Set[Id]]].get

  def addNotice(token: UUID, title: String, message: String): Either[Problem, Id] =
    (loginActor ? AddNotice(token, title, message)).as[Either[Problem, Id]].get

  def getNotice(token: UUID, id: Id): Either[Problem, Notice] = {
    val future = Future.channel(500)
    loginActor.!(GetNotice(token, id))(future)
    future.as[Either[Problem, Notice]].getOrElse(leftTimeout)
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String): Either[Problem, Id] = {
    val future = Future.channel(500)
    loginActor.!(UpdateNotice(token, id, title, message))(future)
    future.as[Either[Problem, Id]].getOrElse(leftTimeout)
  }

  def deleteNotice(token: UUID, id: Id): Option[Problem] = {
    val future = Future.channel(500)
    loginActor.!(DeleteNotice(token, id))(future)
    future.as[Any].map {
      case o: Option[_] => o.asInstanceOf[Option[Problem]]
      case Left(p: Problem) => Some(p)
    }.getOrElse(someTimeout)
  }
}