package edu.agh.lroza.actors

import _root_.java.util.UUID
import edu.agh.lroza.common._
import akka.actor.Actor
import akka.dispatch.Future
import scala.LoginActor.{Logout, Login}
import scala.NoticeActor.{DeleteNotice, UpdateNotice, GetNotice}
import scala.NoticesActor.{ActorId, AddNotice, ListNoticesIds}
import scala.{NoticesActor, LoginActor}

class ActorServerScala extends NoticeBoardServer {
  val loginActor = Actor.actorOf[LoginActor].start()
  val noticesActor = Actor.actorOf(new NoticesActor(loginActor)).start()

  def login(username: String, password: String) =
    (loginActor ? Login(username, password)).as[Either[Problem, UUID]] match {
      case Some(answer) => answer
      case None => Left(ProblemS("Timeout occured"))
    }

  def logout(token: UUID) = (loginActor ? Logout(token)).as[Option[Problem]] match {
    case Some(answer) => answer
    case None => Some(ProblemS("Timeout occured"))
  }

  def listNoticesIds(token: UUID): Either[Problem, Set[Id]] =
    (noticesActor ? ListNoticesIds(token)).as[Either[Problem, Set[Id]]] match {
      case Some(answer) =>
        answer
      case None => Left(ProblemS("Timeout occured"))
    }

  def addNotice(token: UUID, title: String, message: String): Either[Problem, Id] =
    (noticesActor ? AddNotice(token, title, message)).as[Either[Problem, Id]] match {
      case Some(answer) =>
        answer
      case None => Left(ProblemS("Timeout occured"))
    }

  def getNotice(token: UUID, id: Id): Either[Problem, Notice] = id match {
    case ActorId(actorRef) =>
      val future = Future.channel()
      if (actorRef.tryTell(GetNotice(token))(future)) {
        future.as[Either[Problem, Notice]] match {
          case Some(answer) =>
            answer
          case None => Left(ProblemS("Timeout occured"))
        }
      } else {
        Left(ProblemS("There is no such notice '" + id + "'"))
      }
    case _ => Left(ProblemS("There is no such notice '" + id + "'"))
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String): Either[Problem, Id] = id match {
    case ActorId(actorRef) =>
      val future = Future.channel()
      if (actorRef.tryTell(UpdateNotice(token, title, message))(future)) {
        future.as[Either[Problem, Id]] match {
          case Some(answer) =>
            answer
          case None => Left(ProblemS("Timeout occured"))
        }
      } else {
        Left(ProblemS("There is no such notice '" + id + "'"))
      }
    case _ => Left(ProblemS("There is no such notice '" + id + "'"))
  }

  def deleteNotice(token: UUID, id: Id): Option[Problem] = id match {
    case ActorId(actorRef) =>
      val future = Future.channel()
      if (actorRef.tryTell(DeleteNotice(token))(future)) {
        future.as[Option[Problem]] match {
          case Some(answer) =>
            answer
          case None => Some(ProblemS("Timeout occured"))
        }
      } else {
        Some(ProblemS("There is no such notice '" + id + "'"))
      }
    case _ => Some(ProblemS("There is no such notice '" + id + "'"))
  }
}