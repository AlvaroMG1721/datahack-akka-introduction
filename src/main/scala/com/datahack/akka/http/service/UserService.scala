package com.datahack.akka.http.service

import com.datahack.akka.http.model.daos.UserDao
import com.datahack.akka.http.model.dtos.User
import com.datahack.akka.http.service.UserService.{UserServiceResponse, _}

import scala.concurrent.{ExecutionContext, Future}

/*
 * Mensajes de respuesta de los métodos del servicio de usuarios
 */
object UserService {

  trait UserServiceResponse
  case class AllUsers(users: Seq[User]) extends UserServiceResponse
  case class FoundUser(user: User) extends UserServiceResponse
  case object UserNotFound extends UserServiceResponse
  case class StoredUser(user: Option[User]) extends UserServiceResponse
  case class UpdatedUser(user: User) extends UserServiceResponse
  case object UserDeleted extends UserServiceResponse
}

class UserService(userDao: UserDao) {

  def users()(implicit executionContext: ExecutionContext): Future[UserServiceResponse] = {
    userDao.getAll.map(AllUsers)
  }

  def searchUser(id: Long)(implicit executionContext: ExecutionContext): Future[UserServiceResponse] = {
    userDao.getById(id).map(_.map(FoundUser).getOrElse(UserNotFound))
  }

  def insertUser(user: User)(implicit executionContext: ExecutionContext): Future[UserServiceResponse] = {
    for {
      id <- userDao.insert(user)
      user <- userDao.getById(id)
    } yield StoredUser(user)
  }

  def updateUser(user: User)(implicit executionContext: ExecutionContext): Future[UserServiceResponse] = {
    (for {
      userFound <- userDao.getById(user.id.get)
      if userFound.isDefined
      _ <- userDao.update(user)
      updatedUser <- userDao.getById(user.id.get)
    } yield
      updatedUser.map(UpdatedUser).get) recover {
      case _: NoSuchElementException => UserNotFound
      case e: Exception => throw e
    }
  }

  def deleteUser(id: Long)(implicit executionContext: ExecutionContext): Future[UserServiceResponse] =  {
    (for {
      userFound <- userDao.getById(id)
      if userFound.isDefined
      _ <- userDao.delete(id)
    } yield UserDeleted) recover {
      case _: NoSuchElementException => UserNotFound
    }
  }
}
