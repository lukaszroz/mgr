package edu.agh.lroza.locks

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.ShouldMatchers

class SynchronizedServerScalaTest extends FunSuite with BeforeAndAfter with ShouldMatchers {

//  var server: Server = null

  before {
//    start[SynchronizedServerScala]()
//    server = remote.typedActorFor(classOf[Server], "server", "localhost", port)
  }

  ignore("should be able to connect") {
//    assert(server.login("user01", "user02") === None)
//
//    val token = server.login("user01", "user01").get
//    assert(token.name === "user01")
//    assert(server.isLogged(token) === true)
//
//    server.logout(token)
//    assert(server.isLogged(token) === false)
  }

  after {
//    stop()
  }
}