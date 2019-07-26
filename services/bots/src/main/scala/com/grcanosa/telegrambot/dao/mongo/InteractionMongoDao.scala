package com.grcanosa.telegrambot.dao.mongo

import com.grcanosa.telegrambot.dao.InteractionDao
import com.grcanosa.telegrambot.model.Interaction
import org.mongodb.scala.{MongoClient, MongoCollection}

import scala.concurrent.{ExecutionContext, Future}


class InteractionMongoDao(val host: String
                          , val port: Int
                          , val databaseName: String)
                         (implicit val executionContext: ExecutionContext)
extends InteractionDao
with MongoCodecs
with MongoResultsMappings {

  lazy val mongo = MongoClient(s"mongodb://$host:$port")

  lazy val database = mongo.getDatabase(databaseName).withCodecRegistry(codecRegistry)

  lazy val interactions: MongoCollection[InteractionMongo] = database.getCollection("interactions")

  override def insert(interaction: Interaction) = {
    interactions.insertOne(fromInteraction(interaction)).toBooleanFuture()
  }

  override def getAll(): Future[Seq[Interaction]] = {
    interactions.find().toFuture().map(_.map(fromInteractionMongo))
  }

}
