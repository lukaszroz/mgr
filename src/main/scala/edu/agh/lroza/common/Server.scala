package edu.agh.lroza.common

import java.util.UUID
import akka.dispatch.Future

case class Token(name:String, id:UUID)

trait Server{
  def login(name:String, pass:String):Option[Token]
  def logout(token:Token)
  def inc()
  def incF():Future[Int]
  def get:Future[Int]
  def getId:Int
  def isLogged(token:Token):Boolean
}



