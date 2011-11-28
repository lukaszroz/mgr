package edu.agh.lroza.actors

import java.util.UUID
import edu.agh.lroza.common.{ProblemS, Problem}
import akka.actor.{UntypedChannel, Actor}

case class Login(username: String, password: String);

case class Logout(token: UUID);

case class ValidateToken[T](token: UUID, originalSender: UntypedChannel, returnOption: Boolean, returnMessage: AnyRef);

class LoginActorS extends Actor {
  var loggedUsers = Set[UUID]()

  def login(username: String, password: String): Either[Problem, UUID] = {
    if (username == password) {
      val token = UUID.randomUUID()
      loggedUsers = loggedUsers + token
      Right(token)
    } else {
      Left(ProblemS("Wrong password"))
    }
  }

  def logout(token: UUID): Option[Problem] = {
    if (loggedUsers.contains(token)) {
      loggedUsers = loggedUsers - token
      None
    } else {
      Some(ProblemS("Invalid token"))
    }
  }

  protected def receive = {
    case ValidateToken(token, originalSender, returnOption, returnMessage) =>
      if (loggedUsers.contains(token)) {
        self reply returnMessage
      } else {
        if (returnOption) {
          originalSender ! Some(ProblemS("Please log in"))
        } else {
          originalSender ! Left(ProblemS("Please log in"))
        }
      }
    case Login(username, password) => self reply login(username, password)
    case Logout(token) => self reply logout(token)
  }
}