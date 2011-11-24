package edu.agh.lroza.common

import collection.mutable.{Set, HashSet, SynchronizedSet}
import akka.actor.Actor._
import java.util.Scanner
import akka.actor.Actor

object Utils {
  val port = 2552

  def start(server: NoticeBoardServer) {
    remote.start("localhost", port)
    remote.register("server", Actor.actorOf(ServerActor(server)).start())
  }

  def stop() {
    remote.shutdown()
    registry.shutdownAll()
  }

  def getClient = new Client(remote.actorFor("server", "localhost", port))

  def waitAndStop() {
    val sc = new Scanner(System.in);
    while (sc.nextLine() != "") {}
    stop()
  }

  def run(server: NoticeBoardServer) {
    start(server)
    waitAndStop()
  }

  def makeSet[T]: Set[T] = {
    new HashSet[T] with SynchronizedSet[T]
  }
}