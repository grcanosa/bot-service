package com.grcanosa.telegrambot.dao.mongo


import com.grcanosa.telegrambot.dao.BotUserDao
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.model.BotUser.BotUserPermission
import com.mongodb.client.result.UpdateResult
import org.mongodb.scala.{Completed, FindObservable, MongoClient, MongoCollection, SingleObservable, result}
import org.mongodb.scala.model.Filters._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


class BotUserMongoDao(val host: String
                      , val port: Int
                      , val databaseName: String)
                     (implicit val executionContext: ExecutionContext)
extends BotUserDao
with MongoCodecs
with MongoResultsMappings {

  import com.grcanosa.telegrambot.utils.BotUtils._



  lazy val mongo = MongoClient(s"mongodb://$host:$port")

  lazy val database = mongo.getDatabase(databaseName).withCodecRegistry(codecRegistry)

  lazy val users: MongoCollection[BotUserMongo] = database.getCollection("users")


  override def getUsers(): Future[Seq[BotUser]] = {
      BOTLOG.info("Getting all users")
      users.find().toFuture().map(_.map(fromBotUserMongo))
  }

  override def getUser(userId: Long): Future[Option[BotUser]] = {
    users.find(equal("id",userId)).headOption().map(_.map(fromBotUserMongo))
  }

  override def insertUser(user: BotUser): Future[Boolean] = {
    users.insertOne(fromBotUser(user)).toBooleanFuture()
  }

  override def removeUser(user: BotUser): Future[Boolean] = ???

  override def updateUser(user: BotUser): Future[Boolean] = {
    val b: SingleObservable[UpdateResult] = users.replaceOne(equal("id",user.id),fromBotUser(user))
    users.replaceOne(equal("id",user.id),fromBotUser(user)).toBooleanFuture()
  }


}
