package edu.agh.lroza.concept

import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import akka.actor.{Actor, ActorRef}

class ServerClientTest extends FunSuite with MockitoSugar with BeforeAndAfter with ShouldMatchers {
  val server = mock[Server]
  var serverActor: ActorRef = Actor.actorOf(ServerActor(server)).start()
  var serverClient = new ServerClient(serverActor)

  before {
    reset(server)
  }

  test("remove should send Remove to server") {
    serverClient.remove()
    verify(server).remove()
  }

  test("iterate should send Iterate to server") {
    serverClient.iterate()
    verify(server).iterate()
  }
}