package edu.agh.lroza.common

class Topic private(messages: List[Message]);

object Topic {
  def apply(message: String) = new Topic(List(Message(message)))
}