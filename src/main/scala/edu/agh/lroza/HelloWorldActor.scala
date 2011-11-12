package edu.agh.lroza

import akka.actor.Actor
import akka.actor.Actor._

class HelloWorldActor extends Actor {
  def receive = {
    case msg => self reply (msg + " World")
  }
}



