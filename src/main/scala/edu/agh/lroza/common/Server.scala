package edu.agh.lroza.common

import java.util.UUID

trait Server {
  def login(username: String, password: String): UUID

  def listTopics():Iterable[String]

  def logout(token: UUID):Boolean
}



