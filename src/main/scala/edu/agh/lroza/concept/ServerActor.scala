package edu.agh.lroza.concept

import akka.actor.Actor
import akka.dispatch.Future

case object Remove

case object Iterate

class ServerActor(server: Server) extends Actor {
  protected def receive = {
    case Remove => {
      val channel = self.channel
      Future {
        server.remove()
      } onComplete {
        _.value.get.fold(channel ! _,channel ! _)
      }
    }
    case Iterate => {
      val channel = self.channel
      Future {
        server.iterate()
      } onComplete {
        _.value.get.fold(channel ! _,channel ! _)
      }
    }
  }
}

object ServerActor {
  def apply(server: Server) = new ServerActor(server)
}