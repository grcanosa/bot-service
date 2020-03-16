package com.grcanosa.telegrambot.dao.redis

import akka.actor.ActorSystem
import com.grcanosa.telegrambot.dao.BotUserDao
import com.grcanosa.telegrambot.model.BotUser
import redis.RedisClient

import scala.concurrent.Future

class BotUserRedisDao(val redisHost: String
                      , val redisPort: Int
                      , val keyBase: String)
                     (implicit val actorSystem: ActorSystem)
extends BotUserDao{

  implicit val exContext = actorSystem.dispatcher

  lazy val redis = new RedisClient(redisHost,redisPort)

  val usersKey = "users"

  def makeKey(keys: String*) = {
    s"$keyBase:$usersKey:${keys.mkString(":")}"
  }

  override def getUsers(): Future[Seq[BotUser]] = {
    val users1: Future[Seq[Future[Option[BotUser]]]] = getUserIds().map(_.map(getUser))
    val users2: Future[Seq[Option[BotUser]]] = users1.flatMap{
      case s => Future.sequence(s)
    }
    users2.map(_.flatten)
  }

  override def getUser(userId: Long): Future[Option[BotUser]] = {
    existsUserId(userId).flatMap{
      case false => Future(None)
      case true => getUserData(userId)
    }
  }

  override def insertUser(user: BotUser): Future[Boolean] = {
    addUserId(user.id).flatMap{
      case true => addUserData(user)
      case false => Future(false)
    }
  }

  override def removeUser(user: BotUser): Future[Boolean] = ???

  override def updateUser(user: BotUser): Future[Boolean] = {
    existsUserId(user.id).flatMap {
      case false => Future(false)
      case true => addUserData(user)
    }
  }


  private val userIdsKey = makeKey("users_ids")

  private val userAllowedKey = (userId: Long) => makeKey(userId.toString, "allowed")

  private val userNameKey = (userId: Long) => makeKey(userId.toString, "name")

  private def existsUserId(userId: Long) = {
    redis.smembers(userIdsKey).map(_.map(_.utf8String.toLong).count(_ == userId) == 1)
  }


  private def addUserId(userId: Long) = {
    redis.sadd(userIdsKey,userId.toString).map(_ == 1)
  }

  private def getUserIds() = {
    redis.smembers(userIdsKey).map(_.map(_.utf8String.toLong))
  }

  private def addUserData(user: BotUser) = {
    Future.sequence(
      Seq(
        redis.set(userAllowedKey(user.id), user.permission.value),
        redis.set(userNameKey(user.id), user.name)
      )
    ).map {
      _.foldLeft(true)(_ & _)
    }
  }

  private def getUserData(userId: Long): Future[Option[BotUser]] = {
    val f_permission = redis.get(userAllowedKey(userId)).map(_.map(_.utf8String))
    val f_name = redis.get(userNameKey(userId)).map(_.map(_.utf8String))

    val r: Future[(Option[String], Option[String])] = for{
      permission <- f_permission
      name <- f_name
    } yield (permission, name)

    r.map{
      case (Some(permission), Some(name)) => Some(BotUser(userId, BotUser.getPermission(permission), name,None, None))
      case _ => None
    }

  }
}
