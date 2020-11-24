package com.grcanosa.bots.bodabot

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, Props}
import com.bot4s.telegram.methods.SendMessage
import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}
import com.grcanosa.telegrambot.bot.BotWithAdmin
import com.grcanosa.telegrambot.dao.{BotDao, BotUserDao, InteractionDao}
import com.grcanosa.telegrambot.dao.mongo.{BotUserMongoDao, InteractionMongoDao}
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.model.BotUser.PERMISSION_ALLOWED
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

object BodaBot {


  def apply(config: Config)
           (implicit ec: ExecutionContext): BodaBot = {
    val token = config.getString("bot.token")
    val adminId = config.getLong("bot.adminId")

    val mongoHost = config.getString("mongo.host")
    val mongoPort = config.getInt("mongo.port")
    val mongoDatabaseName = config.getString("bot.mongo.databasename")

    val mongoUserDao = new BotUserMongoDao(mongoHost,mongoPort,mongoDatabaseName)

    val mongoInteractionDao = new InteractionMongoDao(mongoHost, mongoPort, mongoDatabaseName)

    implicit object BodaDao extends BotDao{
      override def botUserDao: BotUserDao = mongoUserDao

      override def interactionDao: InteractionDao = mongoInteractionDao
    }
    val consumerKey = config.getString("bot.twitter.consumer-key")
    val consumerSecret = config.getString("bot.twitter.consumer-secret")
    val accessKey = config.getString("bot.twitter.access-key")
    val accessSecret = config.getString("bot.twitter.access-secret")
    val consumerToken = ConsumerToken(key = consumerKey, secret = consumerSecret)
    val accessToken = AccessToken(key = accessKey, secret = accessSecret)

    val restClient = TwitterRestClient(consumerToken, accessToken)

    val bot = new BodaBot(token,adminId,restClient)
    bot
  }

}


class BodaBot(override val token: String
             , override val adminId: Long
             , val twitterClient: TwitterRestClient
             )
             (implicit botDao: BotDao)
extends BotWithAdmin(token, adminId)
  with TwitterMessages
  with BodaBotResponses
{

  override val defaultUserPermission: BotUser.BotUserPermission = PERMISSION_ALLOWED

  val selfActor: ActorRef = system.actorOf(Props(new BodaBotActor()))


  onCommand("/cuando"){ implicit msg =>
    allowedUser(Some("cuando")){ uH =>
      reply(cuandoResponse)
    }
  }

  onCommand("/donde"){ implicit msg =>
    allowedUser(Some("donde")){ uH =>
      reply(dondeResponse)
    }
  }

  class BodaBotActor extends Actor{
    override def receive = {
      twitterBehaviour
        .orElse(bodaBotActorBehaviour)
    }

  }

  def bodaBotActorBehaviour: Receive = {
    case _ =>
  }


  class BodaBotUserActor extends Actor{
    override def receive = {
      case _ =>
    }
  }

  val botUserActor: ActorRef = system.actorOf(Props(new BodaBotUserActor()),s"common_user_actor")

  override def createNewUserActor(botUser: BotUser): ActorRef = {
    //system.actorOf(Props(new GrcanosaBotUserActor(botUser)),s"actor_${botUser.id}")
    botUserActor
  }


}
