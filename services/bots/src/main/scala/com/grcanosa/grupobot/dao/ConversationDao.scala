package com.grcanosa.grupobot.dao

import com.grcanosa.grupobot.model.Conversation
import com.grcanosa.telegrambot.dao.mongo.MongoResultsMappings
import org.mongodb.scala.{MongoClient, MongoCollection}
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}

import scala.concurrent.ExecutionContext



class ConversationDao(val host: String
                      , val port: Int
                      , val databaseName: String)
                     (implicit val executionContext: ExecutionContext) extends MongoResultsMappings{

  import com.grcanosa.telegrambot.utils.BotUtils._

  lazy val mongo = MongoClient(s"mongodb://$host:$port")

  lazy val codecRegistry = fromRegistries(fromProviders(classOf[Conversation]),
    DEFAULT_CODEC_REGISTRY )

  lazy val database = mongo.getDatabase(databaseName).withCodecRegistry(codecRegistry)

  lazy val conversations: MongoCollection[Conversation] = database.getCollection("conversations")

  def addConversation(conversation: Conversation) = {
    conversations.insertOne(conversation).toBooleanFuture()
  }


}
