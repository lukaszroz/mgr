package edu.agh.lroza.common

import akka.dispatch.Future


object UtilsS {
  def getFuture[T] = Future.channel(50)
}