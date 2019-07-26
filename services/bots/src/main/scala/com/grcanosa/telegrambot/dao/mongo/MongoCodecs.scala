package com.grcanosa.telegrambot.dao.mongo

import java.time.LocalDateTime

import com.grcanosa.telegrambot.model.{BotUser, Interaction}
import com.grcanosa.telegrambot.model.BotUser.{BotUserPermission, PERMISSION_ALLOWED, PERMISSION_NOT_ALLOWED, PERMISSION_NOT_SET}
import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.mongodb.scala.bson.ObjectId
import BotUser._

trait MongoCodecs {



  import org.mongodb.scala.bson.codecs.Macros._
  import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
  import org.bson.codecs.configuration.CodecRegistries.{fromRegistries, fromProviders}


  case class BotUserMongo( id: Long, name: String, permission: String, username: Option[String], lastName: Option[String])


  def fromBotUser(bu: BotUser) = {
    BotUserMongo(bu.id,bu.name,bu.permission.value,bu.username,bu.lastName)
  }

  def fromBotUserMongo(bum: BotUserMongo) = {
    BotUser(bum.id,getPermission(bum.permission),bum.name,bum.username,bum.lastName)
  }


  case class InteractionMongo(id:Long, interaction: String, date: String)

  def fromInteraction(in: Interaction) = {
    InteractionMongo(in.id,in.interaction,LocalDateTime.now().toString)
  }

  def fromInteractionMongo(intm: InteractionMongo) = {
    Interaction(intm.id,intm.interaction)
  }


  lazy val codecRegistry = fromRegistries(fromProviders(classOf[BotUserMongo], classOf[InteractionMongo]),
    DEFAULT_CODEC_REGISTRY )



}
