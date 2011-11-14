package edu.agh.lroza.locks

import edu.agh.lroza.common.{Token, Server}
import edu.agh.lroza.common.Utils._
import java.util.{HashSet, UUID}
import akka.actor.{Actor, TypedActor}
import java.util.concurrent._
import collection.mutable.Queue

class SynchronizedServerScala extends TypedActor with Server {

  val users = makeSet[Token]

  def login(name: String, pass: String) = {
    if (name.equals(pass)) {
      val token = Token(name, UUID.randomUUID())
      users += token
      Some(token)
    } else
      None
  }

  def isLogged(token: Token) = users(token)

  def logout(token: Token) {
    users -= token
  }

  var i = new HashSet[Int]()
  var id = 0
  val cdl = new CountDownLatch(1)
  val workQueue = new LinkedBlockingQueue[Runnable]
  val exec = new ThreadPoolExecutor(32,
    32,
    2000,
    TimeUnit.MILLISECONDS,
    workQueue,
    new ThreadPoolExecutor.CallerRunsPolicy)

  def inc() {
    id += 1
    val l_id = id
    //    exec.execute(new Runnable {
    //      def run()
    Actor.spawn {
      if (i.contains(l_id) || i.contains(l_id * 1000000))
        println("----------------ERROR--------------")
      i.add(l_id)
      i.add(l_id * 1000000)
      var rr = false
      val iterator = i.iterator()
      while (iterator.hasNext) {
        val ii = iterator.next()
        if (l_id == ii) {
          rr = true
        }
      }
      if (rr)
        i.remove(l_id)
      else
        println("----------------ERROR--------------")
    }
    //    })
  }

  def incF() = {
    id += 1
    val l_id = id
    //    exec.execute(new Runnable {
    //      def run()
    future {
      println("[" + l_id + "] start")
      if (i.contains(l_id) || i.contains(l_id * 1000000))
        println("----------------ERROR--------------")
      i.add(l_id)
      i.add(l_id * 1000000)
      var rr = false
      val iterator = i.iterator()
      while (iterator.hasNext) {
        val ii = iterator.next()
        if (l_id == ii) {
          rr = true
        }
      }
      if (rr)
        i.remove(l_id)
      else
        println("----------------ERROR--------------")
      println("[" + l_id + "] stop")
      l_id
    }
  }

  def get = {
    id += 1
    val l_id = id
    future {
      println("[" + l_id + ":" + System.currentTimeMillis() + "]sleeping")
      Thread.sleep(200)
      i.size()
    }
  }

  def getId = {
    println("shutting down")
    exec.shutdownNow()
    id
  }
}

object SynchronizedServerScala {
  def main(args: Array[String]) {
    run[SynchronizedServerScala]()
  }
}