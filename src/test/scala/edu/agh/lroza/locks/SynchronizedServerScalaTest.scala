package edu.agh.lroza.locks

import org.scalatest.FunSuite
import akka.actor.Actor._
import edu.agh.lroza.common.Utils._
import edu.agh.lroza.common.Server
import org.scalatest.BeforeAndAfter

class SynchronizedServerScalaTest extends FunSuite with BeforeAndAfter{

  var server:Server = null

  before {
    start[SynchronizedServerScala]()
    server = remote.typedActorFor(classOf[Server], "server", "localhost", port)
  }

  test("should be able to connect") {
    assert(server.login("user01", "user02") === None)

    val token = server.login("user01", "user01").get
    assert(token.name === "user01")
    assert(server.isLogged(token) === true)
    
    server.logout(token)
    assert(server.isLogged(token) === false)
  }

  after{
    stop()
  }
}