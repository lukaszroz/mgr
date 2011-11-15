package edu.agh.lroza.concept

import akka.actor.Actor

case object Remove
case object Iterate

class ServerActor(server: Server) extends Actor {
  protected def receive = {
    case Remove => {
      val channel = self.channel
      Actor.spawn {
        channel ! server.remove()
      }
    }
    case Iterate => {
      val channel = self.channel
      Actor.spawn {
        channel ! server.iterate()
      }
    }
  }
}

object ServerActor {
  def apply(server:Server) = new ServerActor(server)
}