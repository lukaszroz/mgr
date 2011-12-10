package edu.agh.lroza.common

import edu.agh.lroza.synchronize.{SynchronizedServerJava, SynchronizedServerScala}
import edu.agh.lroza.locks.{CustomLocksServerJava, CustomLocksServerScala}
import edu.agh.lroza.concurrent.{ConcurrentServerJava, ConcurrentServerScala}
import edu.agh.lroza.immutable.ImmutableServerScala
import edu.agh.lroza.actors.{ActorServerJava, ActorServerScala}
import org.clapper.argot.{ArgotUsageException, ArgotParser, ArgotConverters}
import scala.Predef._
import java.util.concurrent.{TimeUnit, CyclicBarrier}


object Simulation {
  // Argument specifications

  import ArgotConverters._

  val parser = new ArgotParser(
    "simulation",
    preUsage = Some("Notice Board Simulation: Version 1.0. Copyright (c) " +
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

  val full = parser.flag[Boolean](List("f", "full"), "Full simulation takes longer")

  val users = parser.option[Int](List("u", "users-count"), "users-count", "Number of concurrent users. Default 1.")

  val writeEvery = parser.option[Int](List("w", "write-period"), "write-period", "How many read requests there is for " +
    "one write request. The higher the number, the lower whe write request frequency. Default 0 (no writes).")

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

    val barrier = new CyclicBarrier(users.value.getOrElse(1))
    val writePeriod = writeEvery.value.getOrElse(0)

    val clients = for (i <- 1 to users.value.getOrElse(1)) yield new User(server, i, barrier, writePeriod).start()


    for (i <- 1 to 5) {
      for (j <- warmup) {
        clients.map(_ ! User.Run(j))
        Thread.sleep(100);
      }
    }

    def runSimulation(i: Int) {
      println("--------------------------------------")
      val start = System.nanoTime()
      val results = clients.map(_ !! User.Run(n)).map(_())
      val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)

      val totalCount = results.map(_ match {
        case (_, _, count: Long, _) => count
      }).sum.asInstanceOf[Double]

      results.foreach {
        _ match {
          case (logPrefix: String, duration: Long, count: Long, problemCount: Long) =>
            println("%s duration: %5dms, count: %d, avg: %5.2fμs".format(logPrefix,
              TimeUnit.MICROSECONDS.toMillis(duration), count, (duration.asInstanceOf[Double]) / count))
        }
      }
      println("[simulation%2d] total duration:  %6dms".format(i, duration))
      println("[simulation%2d] total throughput: %5.2frq/ms".format(i, totalCount / duration))
    }

    runSimulation(0)
    println("--------------------------------------")

    for (i <- 1 to 20) {
      runSimulation(i)
    }

    clients.map(_ !! User.Stop).foreach(f => println(f()))

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