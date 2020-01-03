package com.example

import zio.{ RIO, Task, ZIO }
import Domain.User

trait UserRepo {
  val userRepo: UserRepo.Service[Any]
}
object UserRepo {
  trait Service[R] {
    def find(id: Int): RIO[R, User]
  }

}
trait UserRepoLive extends UserRepo {
  val userRepo: UserRepo.Service[Any] = new UserRepo.Service[Any] {
    override def find(id: Int): Task[User] = Task.succeed {
      if (id == 7)
        User(id, "willem")
      else if (id == 1)
        User(id, "virgil")
      else User(id, "unknown")
    }
  }
}

package object userrepo extends UserRepo.Service[UserRepo] {
  override def find(id: Int): RIO[UserRepo, User] = ZIO.accessM(_.userRepo.find(id))
}
