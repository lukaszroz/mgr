package edu.agh.lroza.common

class Topic private(messages: Iterable[Message]);

object Topic {
  def apply(message: String) = new Topic(Iterable(Message(message)))
}