package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import zio.DefaultRuntime

import scala.io.StdIn

object Boot extends App {

  val runtime = new DefaultRuntime() {}
  implicit val ec = runtime.platform.executor.asEC

  implicit val system = ActorSystem(name = "zio-akka-system", defaultExecutionContext = Some(ec))

  class LiveEnv
      extends UserRepoLive

  val liveEnv = new LiveEnv

  val api = new UserRoutes(liveEnv)

  val bindingFuture = Http().bindAndHandle(api.zioRoute, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

}