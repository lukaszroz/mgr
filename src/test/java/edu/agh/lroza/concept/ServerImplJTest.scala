package edu.agh.lroza.concept

import org.scalatest.FunSuite
import java.util.ConcurrentModificationException
import akka.dispatch.Future

class ServerImplJTest extends FunSuite {

  test("should cause exception") {
    val server = new ServerImplJ
    Future {
      Thread.sleep(10)
      for (i <- 1 to 10)
        Future {
          server.remove()
        }
    }
    intercept[ConcurrentModificationException] {
      server.iterate()
    }
  }
}