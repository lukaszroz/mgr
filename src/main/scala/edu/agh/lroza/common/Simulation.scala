package edu.agh.lroza.common

import actors.Actor
import edu.agh.lroza.synchronize.{SynchronizedServerJava, SynchronizedServerScala}
import edu.agh.lroza.locks.{CustomLocksServerJava, CustomLocksServerScala}
import edu.agh.lroza.concurrent.{ConcurrentServerJava, ConcurrentServerScala}
import edu.agh.lroza.immutable.ImmutableServerScala
import edu.agh.lroza.actors.{ActorServerJava, ActorServerScala}
import org.clapper.argot.{ArgotUsageException, ArgotParser, ArgotConverters}


object Simulation {
  // Argument specifications

  import ArgotConverters._

  val parser = new ArgotParser(
    "simulation",
    preUsage = Some("Simutation: Version 1.0. Copyright (c) " +
      "2011, Lukasz W. Rozycki")
  )

  val server = parser.option[NoticeBoardServer](List("s", "server"), "server", """Server implemention to use. Possible values:
  SS - SynchronizedServerScala (default)
  SJ - SynchronizedServerJava
  LS - CustomLocksServerScala
  LJ - CustomLocksServerJava
  CS - ConcurrentServerScala
  CJ - ConcurrentServerJava
  IM - ImmutableServerScala
  AS - ActorServerScala
  AJ - ActorServerJava
  """) {
    case ("SS", _) => new SynchronizedServerScala
    case ("SJ", _) => new SynchronizedServerJava
    case ("LS", _) => new CustomLocksServerScala
    case ("LJ", _) => new CustomLocksServerJava
    case ("CS", _) => new ConcurrentServerScala
    case ("CJ", _) => new ConcurrentServerJava
    case ("IM", _) => new ImmutableServerScala
    case ("AS", _) => new ActorServerScala
    case ("AJ", _) => new ActorServerJava
  }

  val full = parser.flag[Boolean](List("f", "full"), List("q", "quick"), "Full simulation takes longer")

  def runSimulation(server: NoticeBoardServer) {
    println("Starting simulation with server: " + server.getClass.getName)

    val (warmup, n) = if (full.value.getOrElse(false)) {
      (Seq(1, 10, 100, 1000), 1000)
    } else {
      (Seq(1, 10, 100), 100)
    }

    val token = server.login("master", "master").right.get
    for (i <- 1 to n * 10) {
      server.login("a", "a")
    }
    for (i <- 1 to n * 10) {
      server.addNotice(token, "testTitle%4d".format(i), "testMessage%4d".format(i))
    }

    val client = new User(server, "client").start()
    client ! User.Login

    for (i <- 1 to 5) {
      for (j <- warmup) {
        client ! User.ReadAll(j)
        Thread.sleep(100);
      }
    }

    for (i <- 1 to 20) {
      client ! User.ReadAll(n)
    }

    client ! User.Logout
    client ! User.Stop

    Actor.self.receive {
      case s: String => println("Done.")
    }

    akka.actor.Actor.registry.shutdownAll()
  }

  def main(args: Array[String]) {
    try {
      parser.parse(args)
      runSimulation(server.value.getOrElse(new SynchronizedServerScala))
    } catch {
      case e: ArgotUsageException => println(e.message)
    }
  }
}