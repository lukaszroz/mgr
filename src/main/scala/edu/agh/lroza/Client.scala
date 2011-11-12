package edu.agh.lroza

import akka.actor.Actor._
import java.util.Scanner
import java.util.concurrent.atomic.AtomicBoolean
import akka.actor.Actor

class Client extends Runnable {

  val actor = remote.actorFor("hello-service", "localhost", 2552)

  def run() {
    for (i <- 1 to 10) {
      println("[Client] Got: " + (actor ? "Hello").as[String])
    }
  }
}

object Client extends App {
  val actor = remote.actorFor("hello-service", "localhost", 2552)

  val run = new AtomicBoolean(true)
  spawn{
    while(run.get) {
      println("[Client] Got: " + (actor ? "Hello").as[String])
      Thread.sleep(1000)
    }
  }

  val sc = new Scanner(System.in);
  while (sc.nextLine() != "") {}
  run.set(false)
  Actor.registry.shutdownAll()
}