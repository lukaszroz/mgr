package edu.agh.lroza.concept

import java.util.HashSet
import util.Random


class ServerImpl extends Server {
  val set = new HashSet[Int]()
  for (i <- 1 to 1000) set.add(i)

  def remove() = {
    set.remove(Random.nextInt(set.size()))
    set.size()
  }

  def iterate() = {
    var sum = 0
    val iterator = set.iterator()
    while (iterator.hasNext) {
      Thread.sleep(1)
      sum += iterator.next()
    }
    sum
  }
}