package edu.agh.lroza

import akka.actor.Actor
import com.eaio.uuid.UUID


object Main extends App {
  val actorRef = Actor.actorOf(classOf[EchoActor]).start()
  actorRef ! "Hello"
  val id = actorRef.getUuid()
  Actor.actorOf(classOf[SenderActor]).start() ! id

  Thread.sleep(200)
  Actor.registry.shutdownAll()
}

class SenderActor extends Actor {
  protected def receive = {
    case id: UUID =>
      println("[s] Got id: " + id)
      val actorFor = Actor.registry.actorFor(id).get
      actorFor ! "Hello2"
      actorFor.stop()
      println(Actor.registry.actorFor(id).isDefined)
      val b = actorFor tryTell "Are you there?"
      println("[s] try: " + b)
      val future = actorFor ? "Are you there?"
      println("[s] waiting...")
      future.await
      future.value match {
        case Some(Right(v)) => println("Success: " + v)
        case Some(Left(e: Throwable)) => println("Exception: " + e.getMessage)
      }
    case _ => println("[a] Not a string")
  }
}

class EchoActor extends Actor {
  protected def receive = {
    case s: String => println("[a] Got string: " + s)
    case _ => println("[a] Not a string")
  }
}