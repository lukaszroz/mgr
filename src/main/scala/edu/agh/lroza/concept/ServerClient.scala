package edu.agh.lroza.concept

import akka.actor.ActorRef

class ServerClient(server: ActorRef) extends Server {
  def remove() = sendAndGet(Remove)

  def iterate() = sendAndGet(Iterate)

  private def sendAndGet(message: Any) = {
    (server ? message).get match {
      case o: Int => o
      case e: Exception => throw e
    }
  }
}