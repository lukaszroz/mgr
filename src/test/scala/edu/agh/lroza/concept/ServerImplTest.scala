package edu.agh.lroza.concept

import org.scalatest.FunSuite
import akka.actor.Actor
import java.util.ConcurrentModificationException
import akka.dispatch.Future

class ServerImplTest extends FunSuite {

  test("should cause exception") {
    val server = new ServerImpl
    Future {
      for (i <- 1 to 100)
        Future {
          server.remove()
        }
    }
    intercept[ConcurrentModificationException] {
      server.iterate()
    }
  }
}