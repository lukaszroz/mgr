package edu.agh.lroza.common

import java.util.UUID

case class Token(name:String, id:UUID)

trait Server{
  def login(name:String, pass:String):Option[Token]
  def logout(token:Token)
  def isLogged(token:Token):Boolean
}



