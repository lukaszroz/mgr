package edu.agh.lroza.actors

import _root_.java.util.UUID
import edu.agh.lroza.common._
import akka.actor.Actor
import akka.dispatch.Future
import scala.LoginActor.{Logout, Login}
import scala.NoticeActor.{DeleteNotice, UpdateNotice, GetNotice}
import scala.NoticesActor.{ActorId, AddNotice, ListNoticesIds}
import scala.{NoticesActor, LoginActor}
import edu.agh.lroza.scalacommon.{Notice, Problem, NoticeBoardServerScala}

class ActorServerScala extends NoticeBoardServerScala {
  val loginActor = Actor.actorOf[LoginActor].start()
  val noticesActor = Actor.actorOf(new NoticesActor(loginActor)).start()

  val leftTimeout = Left(Problem("Timeout occured"))
  val someTimeout = Some(Problem("Timeout occured"))

  def login(username: String, password: String) =
    (loginActor ? Login(username, password)).as[Either[Problem, UUID]].get

  def logout(token: UUID) = (loginActor ? Logout(token)).as[Option[Problem]].get

  def listNoticesIds(token: UUID): Either[Problem, Set[Id]] =
    (noticesActor ? ListNoticesIds(token)).as[Either[Problem, Set[Id]]].get

  def addNotice(token: UUID, title: String, message: String): Either[Problem, Id] =
    (noticesActor ? AddNotice(token, title, message)).as[Either[Problem, Id]].get

  def getNotice(token: UUID, id: Id): Either[Problem, Notice] = id match {
    case ActorId(actorRef) =>
      val future = Future.channel(500)
      if (actorRef.tryTell(GetNotice(token))(future)) {
        future.as[Either[Problem, Notice]].getOrElse(leftTimeout)
      } else {
        Left(Problem("There is no such notice '" + id + "'"))
      }
    case _ => Left(Problem("There is no such notice '" + id + "'"))
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String): Either[Problem, Id] = id match {
    case ActorId(actorRef) =>
      val future = Future.channel(500)
      if (actorRef.tryTell(UpdateNotice(token, title, message))(future)) {
        future.as[Either[Problem, Id]].getOrElse(leftTimeout)
      } else {
        Left(Problem("There is no such notice '" + id + "'"))
      }
    case _ => Left(Problem("There is no such notice '" + id + "'"))
  }

  def deleteNotice(token: UUID, id: Id): Option[Problem] = id match {
    case ActorId(actorRef) =>
      val future = Future.channel(500)
      if (actorRef.tryTell(DeleteNotice(token))(future)) {
        future.as[Option[Problem]].getOrElse(someTimeout)
      } else {
        Some(Problem("There is no such notice '" + id + "'"))
      }
    case _ => Some(Problem("There is no such notice '" + id + "'"))
  }
}