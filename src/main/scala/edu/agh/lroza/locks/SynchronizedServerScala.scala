package edu.agh.lroza.locks

import edu.agh.lroza.common.Utils._
import java.util.UUID
class SynchronizedServerScala {

//  val users = makeSet[Token]

  def login(name: String, pass: String) = {
    if (name.equals(pass)) {
//      val token = Token(name, UUID.randomUUID())
//      users += token
//      Some(token)
    } else
      None
  }

//  def isLogged(token: Token) = users(token)

//  def logout(token: Token) {
//    users -= token
//  }
}

object SynchronizedServerScala {
  def main(args: Array[String]) {
//    run[SynchronizedServerScala]()
  }
}