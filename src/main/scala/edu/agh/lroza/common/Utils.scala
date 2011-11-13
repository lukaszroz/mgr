package edu.agh.lroza.common

import collection.mutable.{Set, HashSet, SynchronizedSet}
import akka.actor.TypedActor
import akka.actor.Actor._
import java.util.Scanner


object Utils {
  val port = 2552

  def start[T: ClassManifest]() {
    remote.start("localhost", port)
    remote.registerTypedActor("server", TypedActor.newInstance(classOf[Server], classManifest[T].erasure))
  }

  def stop() {
    remote.shutdown()
    registry.shutdownAll()
  }

  def waitAndStop() {
    val sc = new Scanner(System.in);
    while (sc.nextLine() != "") {}
    stop()
  }

  def run[T: ClassManifest]() {
    start[T]()
    waitAndStop()
  }

  def makeSet[T]: Set[T] = {
    new HashSet[T] with SynchronizedSet[T]
  }
}