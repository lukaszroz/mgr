package edu.agh.lroza.common

import akka.actor.{TypedActor, Actor}

abstract class ServerProxy(server: Server) extends TypedActor with Server {
  def login(name: String, pass: String) = server.login(name, pass)

  def logout(token: Token) = null

  def inc() = null

  def get = future(0)

  def getId = 0

  def isLogged(token: Token) = false
}