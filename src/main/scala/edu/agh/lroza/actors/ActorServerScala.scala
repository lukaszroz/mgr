package edu.agh.lroza.actors

import java.util.UUID
import akka.actor.Actor
import edu.agh.lroza.common._

class ActorServerScala extends Server {
  val loginActor = Actor.actorOf[LoginActor].start()
  val topicsActor = Actor.actorOf[TopicsActor].start()

  def login(username: String, password: String) = (loginActor ? Login(username, password)).as[Option[UUID]] match {
    case Some(answer) => answer
    case None => None
  }

  def listTopics(token: UUID) = (topicsActor ? ListTopics(token)).as[Either[Problem, Iterable[String]]] match {
    case Some(answer) => answer
    case None => Left(Problem("Timeout occured"))
  }

  def logout(token: UUID) = (loginActor ? Logout(token)).as[Boolean] match {
    case Some(b) => b
    case None => false
  }
}