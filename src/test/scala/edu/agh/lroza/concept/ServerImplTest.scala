package edu.agh.lroza.concept

import org.scalatest.FunSuite
import akka.actor.Actor
import java.util.ConcurrentModificationException
class ServerImplTest extends FunSuite {

  test("should cause exception") {
    val server = new ServerImpl
    for (i <- 1 to 100)
      Actor.spawn {
        server.remove()
      }
    intercept[ConcurrentModificationException] {
      server.iterate()
    }
  }
}