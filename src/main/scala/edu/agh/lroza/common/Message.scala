package edu.agh.lroza.common

import java.util.concurrent.atomic.AtomicLong


class Message private(id: Long, message: String);

object Message {
  private def getId = new AtomicLong().incrementAndGet()

  def apply(message: String) = new Message(getId, message)
}