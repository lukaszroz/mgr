package edu.agh.lroza.actors

import edu.agh.lroza.common._
import scala.UserActor.Logout
import scala.NoticeActor.{DeleteNotice, UpdateNotice, GetNotice}
import scala.NoticesActor.{AddNotice, ListNoticesIds}
import scala.{NoticesActor, UserActor}
import edu.agh.lroza.scalacommon.{Notice, Problem, NoticeBoardServerScala}
import akka.actor.{ActorRef, Actor, Uuid}
import com.eaio.uuid.UUID

class ActorServerScala extends NoticeBoardServerScala {
  val noticesActor = Actor.actorOf[NoticesActor].start()

  val leftTimeout = Left(Problem("Timeout occured"))
  val someTimeout = Some(Problem("Timeout occured"))

  def getActor(token: UUID) = Actor.registry.actorFor(token.asInstanceOf[Uuid])

  def callActor[T](token: UUID)(code: ActorRef => Either[Problem, T]) = getActor(token).map(code)
    .getOrElse(Left(Problem("Invalid Token")))

  def callActorSome(token: UUID)(code: ActorRef => Option[Problem]) = getActor(token).map(code)
    .getOrElse(Some(Problem("Invalid Token")))

  def login(username: String, password: String) = if (username.equals(password)) {
    Right(Actor.actorOf(new UserActor(noticesActor)).start().getUuid())
  } else {
    Left(Problem("Wrong password"))
  }

  def logout(token: UUID) = callActorSome(token) {
    a =>
      a.?(Logout).as[Option[Problem]].get
  }

  def listNoticesIds(token: UUID): Either[Problem, Set[Id]] = callActor(token) {
    a =>
      (a ? ListNoticesIds(token)).as[Either[Problem, Set[Id]]].get
    //      OrElse(Left(Problem("Invalid Token")))
  }

  def addNotice(token: UUID, title: String, message: String): Either[Problem, Id] = callActor(token) {
    a =>
      (a ? AddNotice(token, title, message)).as[Either[Problem, Id]].get
  }

  def getNotice(token: UUID, id: Id): Either[Problem, Notice] = callActor(token) {
    a =>
      (a ? GetNotice(token, id)).as[Either[Problem, Notice]].getOrElse(leftTimeout)
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String): Either[Problem, Id] = callActor(token) {
    a =>
      (a ? UpdateNotice(token, id, title, message)).as[Either[Problem, Id]].getOrElse(leftTimeout)
  }

  def deleteNotice(token: UUID, id: Id): Option[Problem] = callActorSome(token) {
    a =>
      (a ? DeleteNotice(token, id)).get match {
        case o: Option[_] => o.asInstanceOf[Option[Problem]]
        case Left(p: Problem) => Some(p)
      }
  }
}