package edu.agh.lroza.concept

import akka.dispatch.Future
import akka.actor.{UntypedChannel, Actor}

case object Remove

case object Iterate

class ServerActor(server: Server) extends Actor {
  protected def receive = {
    case Remove => {
      future[Int](server.remove(), self.channel)
    }
    case Iterate => {
      future[Int](server.iterate(), self.channel)
    }
  }

  private def future[T](code: => T, channel:UntypedChannel) = {
      Future {
        code
      } onComplete {
        _.value.get.fold(channel ! _, channel ! _)
      }
  }
}

object ServerActor {
  def apply(server: Server) = new ServerActor(server)
}