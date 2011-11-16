package edu.agh.lroza.concept

import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import akka.actor.Actor
import java.util.ConcurrentModificationException
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, FunSuite}
import akka.dispatch.Future

class RemoteActorTest extends FunSuite with MockitoSugar with BeforeAndAfter with ShouldMatchers with BeforeAndAfterAll {

  test("should cause exception") {
    val server = new ServerImpl
    Utils.start(server)
    var serverClient = Utils.getClient

    Future {
      for (i <- 1 to 10)
        Future {
          serverClient.remove()
        }
    }
    intercept[ConcurrentModificationException] {
      serverClient.iterate()
    }
  }

  override def afterAll() {
    Utils.stop()
  }
}