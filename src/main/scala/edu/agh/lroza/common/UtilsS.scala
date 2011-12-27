package edu.agh.lroza.common

import akka.dispatch.Future


object UtilsS {
  def getFuture = Future.channel(500)
}