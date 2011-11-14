package edu.agh.lroza.locks

import org.scalatest.FunSuite
import akka.actor.Actor._
import edu.agh.lroza.common.Utils._
import edu.agh.lroza.common.Server
import org.scalatest.BeforeAndAfter
import java.util.concurrent._
import org.scalatest.matchers.ShouldMatchers

class SynchronizedServerScalaTest extends FunSuite with BeforeAndAfter with ShouldMatchers {

  var server: Server = null

  before {
    start[SynchronizedServerScala]()
    server = remote.typedActorFor(classOf[Server], "server", "localhost", port)
  }

  ignore("should not return accurate number") {
    for (i <- 1 to 200)
      spawn {
        for (i <- 1 to 10) {
          server.inc()
        }
      }
    println("sleeping...")
    Thread.sleep(2000)
    println("done")
    println("\n[server] counter: " + server.get.get)
    println("\n[server] counter: " + server.get.get)
    println("\n[server] counter: " + server.get.get)
    println("\n[server] counter: " + server.get.get)
    println("\n[server] id: " + server.getId)
    server.get.get should be > 2000
  }

  ignore("should not return accurate number for incF") {
    for (i <- 1 to 200)
      spawn {
        for (i <- 1 to 10) {
          server.incF()
        }
      }
    println("sleeping...")
    Thread.sleep(2000)
    println("done")
    println("\n[server] counter: " + server.get.get)
    println("\n[server] counter: " + server.get.get)
    println("\n[server] counter: " + server.get.get)
    println("\n[server] counter: " + server.get.get)
    println("\n[server] id: " + server.getId)
    server.get.get should be > 2000
  }

  test("all methods should be concurrent") {
    server.inc()
    for (i <- 1 to 20) {
//      println("[server:" + System.currentTimeMillis() + "] counter: " + server.get)
      server.get
    }
    println("[server:" + System.currentTimeMillis() + "]")
    println("\n[server] id: " + server.getId)
    println("[server:" + System.currentTimeMillis() + "]")
  }

  ignore("should be able to connect") {
    assert(server.login("user01", "user02") === None)

    val token = server.login("user01", "user01").get
    assert(token.name === "user01")
    assert(server.isLogged(token) === true)

    server.logout(token)
    assert(server.isLogged(token) === false)
  }

  after {
    stop()
  }
}