//package edu.agh.lroza.actors
//
//import akka.actor.Actor
//import java.util.UUID
//import scala.collection.mutable._
//import edu.agh.lroza.common.{Logout, Login}
//
//case class IsLogged(token: UUID);
//
//class LoginActorS extends Actor {
//  val loggedUsers = Map[UUID, String]()
//
//  def login(username: String, password: String) = {
//    if (username == password) {
//      val token = UUID.randomUUID()
//      loggedUsers += token -> username
//      Some(token)
//    } else {
//      None
//    }
//  }
//
//  def logout(uuid: UUID) = {
//    loggedUsers remove uuid match {
//      case Some(_: String) => true
//      case None => false
//    }
//  }
//
//  protected def receive = {
//    case IsLogged(token) => self reply loggedUsers.contains(token)
//    case Login(username, password) => self reply login(username, password)
//    case Logout(token) => self reply logout(token)
//  }
//}