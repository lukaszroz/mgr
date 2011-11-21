package edu.agh.lroza.common

import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import akka.actor.{ActorRef, Actor}
import java.util.UUID


class ServerActorTest extends FunSuite with MockitoSugar with BeforeAndAfter with ShouldMatchers {
  val server = mock[Server]
  var serverActor: ActorRef = Actor.actorOf(ServerActor(server))

  before {
    reset(server)
    serverActor = Actor.actorOf(ServerActor(server)).start()
  }

  test("login should call server object") {
    (serverActor ? Login("a", "b")).get
    verify(server).login("a", "b")
  }

  test("logout should call server object") {
    val uuid = UUID.randomUUID()
    (serverActor ? Logout(uuid)).get
    verify(server).logout(uuid)
  }

  test("list should call server object") {
    (serverActor ? ListTopics(null)).get
    verify(server).listTopics(null)
  }

  test("addTopic should call server object") {
    (serverActor ? AddTopic(null, null, null)).get
    verify(server).addTopic(null, null, null)
  }

  test("getTopic should call server object") {
    (serverActor ? GetTopic(null, null)).get
    verify(server).getTopic(null, null)
  }

  test("updateTopic should call server object") {
    (serverActor ? UpdateTopicTitle(null, null, null)).get
    verify(server).updateTopicTitle(null, null, null)
  }

  after {
    serverActor.stop()
  }
}