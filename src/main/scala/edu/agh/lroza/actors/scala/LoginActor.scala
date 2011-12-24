package edu.agh.lroza.actors.scala

import java.util.UUID
import akka.actor.{UntypedChannel, Actor}
import edu.agh.lroza.actors.scala.LoginActor.{Logout, Login, ValidateToken}
import edu.agh.lroza.scalacommon.Problem

class LoginActor extends Actor {
  var loggedUsers = Set[UUID]()

  def login(username: String, password: String): Either[Problem, UUID] = if (username == password) {
    val token = UUID.randomUUID()
    loggedUsers = loggedUsers + token
    Right(token)
  } else {
    Left(Problem("Wrong password"))
  }


  def logout(token: UUID): Option[Problem] = if (loggedUsers.contains(token)) {
    loggedUsers = loggedUsers - token
    None
  } else {
    Some(Problem("Invalid token"))
  }


  def returnProblem(returnOption: Boolean, originalSender: UntypedChannel, problem: Problem) {
    if (returnOption) {
      originalSender ! Some(problem)
    } else {
      originalSender ! Left(problem)
    }
  }

  protected def receive = {
    case ValidateToken(token, originalSender, returnOption, returnMessage) =>
      if (loggedUsers.contains(token)) {
        if (!self.channel.tryTell(returnMessage)) {
          returnProblem(returnOption, originalSender, Problem("Notice has been deleted"))
        }
      } else {
        returnProblem(returnOption, originalSender, Problem("Please log in"))
      }
    case Login(username, password) => self reply login(username, password)
    case Logout(token) => self reply logout(token)
  }
}

object LoginActor {

  case class Login(username: String, password: String);

  case class Logout(token: UUID);

  private[actors] case class ValidateToken(token: UUID, originalSender: UntypedChannel, returnOption: Boolean, returnMessage: AnyRef);
}