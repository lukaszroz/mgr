package edu.agh.lroza

import akka.actor.Actor._
import java.io.{InputStreamReader, BufferedReader}
import java.util.Scanner

class PingServer {

  remote.start("localhost", 2552).register(
    "hello-service", actorOf[HelloWorldActor])

  def shutdown() {
    remote.shutdown()
  }
}

object PingServer {
  def main(args: Array[String]) {
    val server = new PingServer
    val sc = new Scanner(System.in);
    while (sc.nextLine() != "") {}
    server.shutdown()
  }
}