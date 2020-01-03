package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.Domain.User
import zio.{ DefaultRuntime, Task, ZIO }

class UserRoutes(env: UserRepo) extends DefaultRuntime {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  val zioRoute: Route =
    path("zio") {
        get {
          complete(unsafeRun(ZIO.succeed("42")))
        }
    } ~
      pathPrefix("test") {
        path(Segment) { id =>
          get {
            complete(unsafeRun(ZIO.succeed(User(id.toInt, "Willem"))))
          }
        }
      }  ~
      pathPrefix("fail") {
        get {
          val task: Task[String] = Task.fail(new RuntimeException("Kaboom"))
          val x = unsafeRun(task.fold(
            ex => ex.getMessage,
            s => s
          ))
          complete(x)
        }
      } ~
      pathPrefix("repo") {
        path(Segment) { id =>
          get {
            val user = userrepo.find(id.toInt).provide(env)
            val y = unsafeRun(user)
            complete(y)
          }
        }
      }

}
