package edu.agh.lroza.actors.scala

import java.util.UUID
import edu.agh.lroza.common.{ProblemS, Problem}
import akka.actor.{UntypedChannel, Actor}
import edu.agh.lroza.actors.scala.LoginActor.{Logout, Login, ValidateToken}

class LoginActor extends Actor {
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
          returnProblem(returnOption, originalSender, ProblemS("Notice has been deleted"))
        }
      } else {
        returnProblem(returnOption, originalSender, ProblemS("Please log in"))
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