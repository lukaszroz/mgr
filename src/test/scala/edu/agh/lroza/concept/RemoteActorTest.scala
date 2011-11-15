package edu.agh.lroza.concept

import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import akka.actor.{Actor, ActorRef}
import Actor._
import java.util.ConcurrentModificationException

class RemoteActorTest extends FunSuite with MockitoSugar with BeforeAndAfter with ShouldMatchers {

  test("should cause exception") {
    val server = new ServerImpl
    var serverActor: ActorRef = Actor.actorOf(ServerActor(server)).start()

    val port = 2552
    remote.start("localhost", port)
    remote.register("server", serverActor)

    var serverClient = new ServerClient(remote.actorFor("server", "localhost", port))

    Actor.spawn {
      for (i <- 1 to 10)
        Actor.spawn {
          serverClient.remove()
        }
    }
    intercept[ConcurrentModificationException] {
      serverClient.iterate()
    }
  }

  after {
    remote.shutdown()
    Actor.registry.shutdownAll()
  }
}