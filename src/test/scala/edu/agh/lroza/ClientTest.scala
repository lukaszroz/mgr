package edu.agh.lroza

import org.scalatest.FunSuite
import concurrent.TaskRunners

class ClientTest extends FunSuite {

  test("client with ping server") {
    val server = new PingServer
    new Client().run()
    server.shutdown()
  }
}