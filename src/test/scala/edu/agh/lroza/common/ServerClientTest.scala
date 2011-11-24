package edu.agh.lroza.common

import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import akka.actor.{Actor, ActorRef}
import java.util.UUID

class ServerClientTest extends FunSuite with MockitoSugar with BeforeAndAfter with ShouldMatchers {
  val server = mock[NoticeBoardServer]
  val serverActor: ActorRef = Actor.actorOf(ServerActor(server)).start()
  val serverClient = new Client(serverActor)
  val uuid = UUID.randomUUID()

  before {
    reset(server)
  }

  test("login should call server object") {
    when(server.login("a", "b")).thenReturn(Some(uuid))
    val uuid1 = serverClient.login("a", "b")
    uuid1.get should equal(uuid)
    verify(server).login("a", "b")
  }

  test("logout should call server object") {
    when(server.logout(uuid)).thenReturn(true)
    serverClient.logout(uuid) should equal(true)
    verify(server).logout(uuid)
  }

  test("list should call server object") {
    when(server.listNoticesIds(null)).thenReturn(Right(Set[String]()))
    serverClient.listNoticesIds(null) should equal(Right(Set[String]()))
    verify(server).listNoticesIds(null)
  }

  test("addTopic should call server object") {
    val topic = Right(Notice(""))
    when(server.addNotice(null, null, null)).thenReturn(topic)
    serverClient.addNotice(null, null, null) should equal(topic)
    verify(server).addNotice(null, null, null)
  }

  test("getTopic should call server object") {
    val topic = Right(Notice(""))
    when(server.getNotice(null, null)).thenReturn(topic)
    serverClient.getTopic(null, null) should equal(topic)
    verify(server).getNotice(null, null)
  }

  test("updateTopic should call server object") {
    val topic = Right(Notice(""))
    when(server.updateTopicTitle(null, null, null)).thenReturn(topic)
    serverClient.updateTopicTitle(null, null, null) should equal(topic)
    verify(server).updateTopicTitle(null, null, null)
  }
}