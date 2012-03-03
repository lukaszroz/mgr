package edu.agh.lroza.concurrent

import edu.agh.lroza.common.Id
import java.util.concurrent.atomic.AtomicLong


case class LongId(id: Long) extends Id

object LongId {
  private val generator = new AtomicLong();

  def apply(): Id = LongId(generator.getAndIncrement)
}