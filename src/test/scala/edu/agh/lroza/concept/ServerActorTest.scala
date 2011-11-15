package edu.agh.lroza.concept

import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import akka.actor.{ActorRef, Actor}


class ServerActorTest extends FunSuite with MockitoSugar with BeforeAndAfter with ShouldMatchers {
  val server = mock[Server]
  var serverActor:ActorRef = Actor.actorOf(ServerActor(server))

  before {
    reset(server)
    serverActor = Actor.actorOf(ServerActor(server)).start()
  }

  test("remove should call server object") {
    (serverActor ? Remove).get
    verify(server).remove()
  }

  test("iterate should call server object") {
    (serverActor ? Remove).get
    verify(server).remove()
  }

  test("should return 2") {
    when(server.remove()).thenReturn(2)
    val future = serverActor ? Remove
    val two = future.get.asInstanceOf[Int]
    expect(2)(two)
    verify(server).remove()
  }

  after {
    serverActor.stop()
  }
}