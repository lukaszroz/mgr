package edu.agh.lroza.actors

import java.util.UUID
import edu.agh.lroza.common._
import akka.actor.Actor
import akka.event.EventHandler
import akka.dispatch.Future

class ActorServerScala extends NoticeBoardServer {
  val loginActor = Actor.actorOf[LoginActorS].start()
  val noticesActor = Actor.actorOf(new NoticesActorS(loginActor)).start()

  def login(username: String, password: String) = (loginActor ? Login(username, password)).as[Either[Problem, UUID]] match {
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
        EventHandler.debug(this, "listNoticesIds(" + token + ")=" + answer)
        answer
      case None => Left(ProblemS("Timeout occured"))
    }

  def addNotice(token: UUID, title: String, message: String): Either[Problem, Id] =
    (noticesActor ? AddNotice(token, title, message)).as[Either[Problem, Id]] match {
      case Some(answer) =>
        EventHandler.debug(this, "addNotice(" + title + ", " + message + ")=" + answer)
        answer
      case None => Left(ProblemS("Timeout occured"))
    }

  def getNotice(token: UUID, id: Id): Either[Problem, Notice] = id match {
    case ActorId(actorRef) =>
      val future = Future.channel()
      if (actorRef.tryTell(GetNotice(token))(future)) {
        future.as[Either[Problem, Notice]] match {
          case Some(answer) =>
            EventHandler.debug(this, "getNotice(" + id + ")=" + answer)
            answer
          case None => Left(ProblemS("Timeout occured"))
        }
      } else {
        Left(ProblemS("There is no such notice '" + id + "'"))
      }
    case _ => Left(ProblemS("There is no such notice '" + id + "'"))
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String): Either[Problem, Id] = id match {
    case ActorId(actorRef) => if (actorRef.isRunning) {
      (actorRef ? UpdateNotice(token, title, message)).as[Either[Problem, Id]] match {
        case Some(answer) =>
          EventHandler.debug(this, "updateNotice(" + id + ", " + title + ", " + message + ")=" + answer)
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
      if (actorRef.isRunning) {
        (actorRef ? DeleteNotice(token)).as[Option[Problem]] match {
          case Some(answer) =>
            EventHandler.debug(this, "deleteNotice(" + id + ")=" + answer)
            answer
          case None => Some(ProblemS("Timeout occured"))
        }
      } else {
        Some(ProblemS("There is no such notice '" + id + "'"))
      }
    case _ => Some(ProblemS("There is no such notice '" + id + "'"))
  }
}

object ActorServerScala {
  def apply() = new ActorServerScala
}