package edu.agh.lroza.concept

import org.scalatest.FunSuite
import akka.actor.Actor
import java.util.ConcurrentModificationException
class ServerImplJTest extends FunSuite {

  test("should cause exception") {
    val server = new ServerImplJ
    Actor.spawn {
      for (i <- 1 to 10)
        Actor.spawn {
          server.remove()
        }
    }
    intercept[ConcurrentModificationException] {
      server.iterate()
    }
  }
}