package edu.agh.lroza.concept

import akka.actor.ActorRef

class ServerClient(server: ActorRef) extends Server {
  def remove() = (server ? Remove).get match {
    case i: Int => i
    case e: Exception => throw e
  }

  def iterate() = (server ? Iterate).get match {
    case i: Int => i
    case e: Exception => throw e
  }
}