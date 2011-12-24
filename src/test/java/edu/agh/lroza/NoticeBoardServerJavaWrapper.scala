package edu.agh.lroza

import common.Id
import javacommon.{ProblemException, NoticeBoardServerJava}
import java.util.UUID
import collection.JavaConversions
import scalacommon.{Notice, Problem, NoticeBoardServerScala}

class NoticeBoardServerJavaWrapper(val server: NoticeBoardServerJava) extends NoticeBoardServerScala {
  def login(username: String, password: String) = try {
    Right(server.login(username, password))
  } catch {
    case problem: ProblemException => Left(Problem(problem.getMessage))
  }

  def logout(token: UUID) = try {
    server.logout(token)
    None
  } catch {
    case problem: ProblemException => Some(Problem(problem.getMessage))
  }

  def listNoticesIds(token: UUID) = try {
    Right(JavaConversions.asScalaSet(server.listNoticesIds(token)))
  } catch {
    case problem: ProblemException => Left(Problem(problem.getMessage))
  }

  def addNotice(token: UUID, title: String, message: String) = try {
    Right(server.addNotice(token, title, message))
  } catch {
    case problem: ProblemException => Left(Problem(problem.getMessage))
  }

  def getNotice(token: UUID, id: Id) = try {
    val javaNotice = server.getNotice(token, id)
    Right(Notice(javaNotice.getTitle, javaNotice.getMessage))
  } catch {
    case problem: ProblemException => Left(Problem(problem.getMessage))
  }

  def updateNotice(token: UUID, id: Id, title: String, message: String) = try {
    Right(server.updateNotice(token, id, title, message))
  } catch {
    case problem: ProblemException => Left(Problem(problem.getMessage))
  }

  def deleteNotice(token: UUID, id: Id) = try {
    server.deleteNotice(token, id)
    None
  } catch {
    case problem: ProblemException => Some(Problem(problem.getMessage))
  }
}