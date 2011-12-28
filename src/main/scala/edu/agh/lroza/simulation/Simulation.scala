package edu.agh.lroza.simulation

import edu.agh.lroza.immutable.ImmutableServerScala
import org.clapper.argot.{ArgotUsageException, ArgotParser, ArgotConverters}
import scala.Predef._
import java.util.concurrent.{TimeUnit, CyclicBarrier}
import java.util.Locale
import collection.mutable.ListBuffer
import edu.agh.lroza.scalacommon.NoticeBoardServerScala
import edu.agh.lroza.synchronize.{SynchronizedServerJava, SynchronizedServerScala}
import edu.agh.lroza.locks.{CustomLocksServerJava, CustomLocksServerScala}
import edu.agh.lroza.concurrent.{ConcurrentServerJava, ConcurrentServerScala}
import edu.agh.lroza.actors.{ActorServerJava, ActorServerScala}
import edu.agh.lroza.javacommon.NoticeBoardServerJava
import actors.Actor


object Simulation {
  // Argument specifications

  import ArgotConverters._

  val parser = new ArgotParser(
    "simulation",
    preUsage = Some("Notice Board Simulation: Version 1.0. Copyright (c) " +
      "2011, Lukasz W. Rozycki")
  )

  val server = parser.parameter[Any]("SERVER_TYPE", """Server implemention to use. Possible values:
  SS - SynchronizedServerScala
  SJ - SynchronizedServerJava
  LS - CustomLocksServerScala
  LJ - CustomLocksServerJava
  CS - ConcurrentServerScala
  CJ - ConcurrentServerJava
  IM - ImmutableServerScala
  AS - ActorServerScala
  AJ - ActorServerJava
  """, false) {
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

  val quick = parser.flag[Boolean](List("q", "quick"), "Quick simulation is very short")

  val users = parser.option[Int](List("u", "users-count"), "count", "Number of concurrent users. Default 1.")

  val writeEvery = parser.option[Int](List("w", "write-period"), "period", "How many read requests there is for " +
    "one write request. The higher the number, the lower whe write request frequency. Default 0 (no writes).")

  def runSimulation(server: Either[NoticeBoardServerJava, NoticeBoardServerScala]) {
    println("Starting simulation with server: " + server.merge.getClass.getName)

    val (warmup, n) = if (quick.value.getOrElse(false)) {
      (Seq(1), 1)
    } else if (full.value.getOrElse(false)) {
      (Seq(1, 10, 100), 100)
    } else {
      (Seq(1, 10), 10)
    }

    val loginScala = (server: NoticeBoardServerScala) => {
      for (i <- 1 to n * 100) {
        server.login("a", "a")
      }
      server.login("master", "master").right.get
    }

    val loginJava = (server: NoticeBoardServerJava) => {
      for (i <- 1 to n * 100) {
        server.login("a", "a")
      }
      server.login("master", "master")
    }

    val token = server.fold(loginJava, loginScala)

    var start = 0L
    val barrier = new CyclicBarrier(users.value.getOrElse(1), new Runnable {
      def run() {
        start = System.nanoTime();
      }
    })

    val writePeriod = writeEvery.value.getOrElse(0)

    val userCount = users.value.getOrElse(1)

    val usersScala = (server: NoticeBoardServerScala) => {
      for (i <- 1 to userCount) yield new ScalaUser(server, i, barrier, writePeriod).start()
    }

    val usersJava = (server: NoticeBoardServerJava) => {
      for (i <- 1 to userCount) yield new JavaUser(server, i, barrier, writePeriod).start()
    }

    val clients: Seq[Actor] = server.fold(usersJava, usersScala)

    val resetScala = (server: NoticeBoardServerScala) => {
      val iterator = server.listNoticesIds(token).right.get.iterator
      while (iterator.hasNext) {
        server.deleteNotice(token, iterator.next())
      }
      for (i <- 1 to n * 100) {
        server.addNotice(token, "testTitle%05d".format(i), "testMessage%5d".format(i))
      }
      System.gc()
    }

    val resetJava = (server: NoticeBoardServerJava) => {
      val iterator = server.listNoticesIds(token).iterator()
      while (iterator.hasNext) {
        server.deleteNotice(token, iterator.next())
      }
      for (i <- 1 to n * 100) {
        server.addNotice(token, "testTitle%05d".format(i), "testMessage%5d".format(i))
      }
      System.gc()
    }

    def reset() {
      server.fold(resetJava, resetScala)
    }

    println("--------warmup------------------------")
    for (i <- 1 to 5) {
      for (j <- warmup) {
        reset()
        val n = if (writePeriod == 10) (j / 10) + 1 else j
        //        println("i: %d, n: %d".format(i, n))
        clients.map(_ !! User.Run(n)).map(_())
        Thread.sleep(100);
      }
    }

    case class Result(totalDuration: Long, totalCount: Long, sumOfDurations: Long);

    val results = ListBuffer[Result]()

    def runSimulation(i: Int) {
      println("--------simulation:%02d------------------".format(i))
      reset()
      val nn = if (writePeriod == 10) (n / userCount * 2) else n
      val currentResults = clients.map(_ !! User.Run(nn)).map(_())
      val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)

      val totalCount = currentResults.map(_ match {
        case (_, _, count: Long, _, _, _) => count
      }).sum

      val sumOfDurations = currentResults.map(_ match {
        case (_, duration: Long, _, _, _, _) => duration
      }).sum

      currentResults.foreach {
        _ match {
          case (logPrefix, duration: Long, count: Long, problemCount: Long, addCount, updateCount) =>
            println("%s duration: %5dms, count: %d, avg: %5.2fμs".format(logPrefix,
              TimeUnit.MICROSECONDS.toMillis(duration), count, (duration.asInstanceOf[Double]) / count))
            println("%s %5d problems (%2.2f%%), %5d adds, %5d updates".format(logPrefix, problemCount, problemCount * 100.0 / count, addCount, updateCount))
        }
      }
      println("[simulation%2d] total duration:  %6dms".format(i, duration))
      println("[simulation%2d] total throughput: %5.2frq/ms".format(i, totalCount * 1.0 / duration))
      println("[simulation%2d] avg response time: %5.2fμs".format(i, sumOfDurations * 1.0 / totalCount))
      results += Result(duration, totalCount, sumOfDurations)
    }

    Locale.setDefault(Locale.US)

    runSimulation(0)

    for (i <- 1 to 20) {
      runSimulation(i)
    }

    clients.map(_ !! User.Stop).foreach(f => println(f()))

    akka.actor.Actor.registry.shutdownAll()

    println("duration [ms]; count [1]; sum of durations [μs]")
    results.foreach {
      case Result(a, b, c) => println("%d;%d;%d".format(a, b, c))
    }
    sys.exit()
  }

  def main(args: Array[String]) {
    try {
      parser.parse(args)
      server.value.get match {
        case s: NoticeBoardServerJava => runSimulation(Left(s))
        case s: NoticeBoardServerScala => runSimulation(Right(s))
      }
    } catch {
      case e: ArgotUsageException => println(e.message)
    }
  }
}