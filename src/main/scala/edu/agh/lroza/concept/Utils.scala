package edu.agh.lroza.concept

import collection.mutable.{Set, HashSet, SynchronizedSet}
import akka.actor.Actor._
import java.util.Scanner
import akka.actor.{Actor, ActorRef}

object Utils {
  val port = 2552

  def start(server: Server) {
    var serverActor: ActorRef = Actor.actorOf(ServerActor(server)).start()

    remote.start("localhost", port)
    remote.register("server", serverActor)
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

  //  def run[T: ClassManifest]() {
  //    start[T]()
  //    waitAndStop()
  //  }

  def makeSet[T]: Set[T] = {
    new HashSet[T] with SynchronizedSet[T]
  }
}