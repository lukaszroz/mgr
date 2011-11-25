//package edu.agh.lroza.actors
//
//import akka.actor.Actor
//import java.util.UUID
//import edu.agh.lroza.common.{Problem, ListTopics}
//
//class TopicsActorS extends Actor {
//  val topics = List[String]()
//  val loginActor = Actor.registry.actorsFor(classOf[LoginActorS])(0)
//
//  def listTopics(token: UUID) = {
//    Either.cond(isLogged(token), topics, Problem("Please log in"))
//  }
//
//  private def isLogged(token: UUID) = (loginActor ? IsLogged(token)).as[Boolean].get
//
//  protected def receive = {
//    case ListTopics(token) => self reply listTopics(token)
//  }
//}