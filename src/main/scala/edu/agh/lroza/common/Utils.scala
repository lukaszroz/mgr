package edu.agh.lroza.common

import collection.mutable.{Set, HashSet, SynchronizedSet}
import akka.actor.Actor._
import java.util.Scanner
import akka.actor.{Actor, ActorRef, TypedActor}

object Utils {
  val port = 2552

  def start(server: Server) {
    remote.start("localhost", port)
    remote.register("server", Actor.actorOf(ServerActor(server)).start())
  }

  def stop() {
    remote.shutdown()
    registry.shutdownAll()
  }

  def getClient = new ServerClient(remote.actorFor("server", "localhost", port))

  def waitAndStop() {
    val sc = new Scanner(System.in);
    while (sc.nextLine() != "") {}
    stop()
  }

  def run(server: Server) {
    start(server)
    waitAndStop()
  }

  def makeSet[T]: Set[T] = {
    new HashSet[T] with SynchronizedSet[T]
  }
}