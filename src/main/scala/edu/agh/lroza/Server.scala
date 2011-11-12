package edu.agh.lroza


trait Server {

  def request(message:String, client:Client)
}