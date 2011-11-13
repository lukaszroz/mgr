package edu.agh.lroza.locks

import akka.actor.Actor._
import java.util.{Scanner, UUID}
import akka.actor.{Actor, TypedActor}
import edu.agh.lroza.common.{Token, Server}
import edu.agh.lroza.common.Utils._

class SynchronizedServerScala extends TypedActor with Server {

  val users = makeSet[Token]

  def login(name: String, pass: String) = {
    if (name.equals(pass)) {
      val token = Token(name, UUID.randomUUID())
      users += token
      Some(token)
    } else
      None
  }

  def isLogged(token:Token) = users(token)

  def logout(token: Token) {
    users -= token
  }
}

object SynchronizedServerScala {
  def main(args: Array[String]) {
    run[SynchronizedServerScala]()
  }
}