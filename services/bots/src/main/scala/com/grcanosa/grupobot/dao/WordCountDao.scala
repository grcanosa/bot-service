package com.grcanosa.grupobot.dao

import com.grcanosa.grupobot.model.{Conversation, WordCount}
import com.grcanosa.telegrambot.dao.mongo.MongoResultsMappings
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.{MongoClient, MongoCollection}

import scala.concurrent.{ExecutionContext, Future}


class WordCountDao(val host: String
                      , val port: Int
                      , val databaseName: String)
                     (implicit val executionContext: ExecutionContext) extends MongoResultsMappings{

  import org.mongodb.scala.model.Filters._
  import org.mongodb.scala.model.Updates._
  lazy val mongo = MongoClient(s"mongodb://$host:$port")

  lazy val codecRegistry = fromRegistries(fromProviders(classOf[WordCount]),
                          DEFAULT_CODEC_REGISTRY )

  lazy val database = mongo.getDatabase(databaseName).withCodecRegistry(codecRegistry)

  lazy val wordCount: MongoCollection[WordCount] = database.getCollection("words")

  def increaseWordCount(word: String) = {
    wordCount.updateOne(equal("word",word),inc("count",1))
        .toFuture()
        .flatMap{
          case ur if ur.getMatchedCount == 0 => wordCount.insertOne(WordCount(word,1)).toBooleanFuture()
          case _ => Future(true)
        }
  }


}
