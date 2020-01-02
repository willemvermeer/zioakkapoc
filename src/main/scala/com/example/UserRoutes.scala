package com.example

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ ActorRef, ActorSystem }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.example.UserRegistry._
import zio.{ DefaultRuntime, Task, ZIO }

import scala.concurrent.Future

class UserRoutes(userRegistry: ActorRef[UserRegistry.Command])(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getUsers(): Future[Users] =
    userRegistry.ask(GetUsers)
  def getUser(name: String): Future[GetUserResponse] =
    userRegistry.ask(GetUser(name, _))
  def createUser(user: User): Future[ActionPerformed] =
    userRegistry.ask(CreateUser(user, _))
  def deleteUser(name: String): Future[ActionPerformed] =
    userRegistry.ask(DeleteUser(name, _))

  val zioRuntime = new DefaultRuntime {}

  val zioRoute: Route =
    path("zio") {
        get {
          complete(zioRuntime.unsafeRun(ZIO.succeed("42")))
        }
    } ~
      pathPrefix("test") {
        path(Segment) { id =>
          get {
            val x = zioRuntime.unsafeRun(ZIO.succeed(User("Willem", 49, id)))
            complete(x)
            // could have been a one-liner:
//            complete(zioRuntime.unsafeRun(ZIO.succeed(User("Willem", 49, id))))
            // but the following doesn't even compile:
//            val x = ZIO.succeed(User("Willem", 49, id))
//            complete(zioRuntime.unsafeRun(x))
            // whereas the following is OK:
//            val x = ZIO.succeed(User("Willem", 49, id))
//            val y = zioRuntime.unsafeRun(x)
//            complete(y)
          }
        }
      }  ~
      pathPrefix("fail") {
        get {
          val task: Task[String] = Task.fail(new RuntimeException("Kaboom"))
          val x = zioRuntime.unsafeRun(task.fold(
            ex => ex.getMessage,
            s => s
          ))
          complete(x)
        }
      }

  val userRoutes: Route =
    pathPrefix("users") {
      concat(
        pathEnd {
          concat(
            get {
              complete(getUsers())
            },
            post {
              entity(as[User]) { user =>
                onSuccess(createUser(user)) { performed =>
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        },
        path(Segment) { name =>
          concat(
            get {
              rejectEmptyResponse {
                onSuccess(getUser(name)) { response =>
                  complete(response.maybeUser)
                }
              }
            },
            delete {
              onSuccess(deleteUser(name)) { performed =>
                complete((StatusCodes.OK, performed))
              }
            })
        })
    }
}
