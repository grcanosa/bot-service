package com.grcanosa.bots.grupobot.dao

import com.grcanosa.bots.grupobot.model.Conversation
import com.grcanosa.telegrambot.dao.mongo.MongoResultsMappings
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry

import scala.concurrent.ExecutionContext

object ConversationDao{

  case class ConversationMongo(id1: Long, name1: String, id2: Long, name2: String, start: String, end: String)

  def toConversationMongo(conv: Conversation) = {
    ConversationMongo(conv.uh1.user.id,conv.uh1.user.name,conv.uh2.user.id,conv.uh2.user.name
          ,conv.start,conv.end)
  }

}

class ConversationDao(val host: String
                      , val port: Int
                      , val databaseName: String)
                     (implicit val executionContext: ExecutionContext) extends MongoResultsMappings{

  import com.grcanosa.telegrambot.utils.BotUtils._

  import ConversationDao._

  lazy val mongo: MongoClient = MongoClient(s"mongodb://$host:$port")

  lazy val codecRegistry: CodecRegistry = fromRegistries(fromProviders(classOf[ConversationMongo]),
    DEFAULT_CODEC_REGISTRY )

  lazy val database: MongoDatabase = mongo.getDatabase(databaseName).withCodecRegistry(codecRegistry)

  lazy val conversations: MongoCollection[ConversationMongo] = database.getCollection("conversations")

  def addConversation(conversation: Conversation) = {
    conversations.insertOne(toConversationMongo(conversation)).toBooleanFuture()
  }


}
