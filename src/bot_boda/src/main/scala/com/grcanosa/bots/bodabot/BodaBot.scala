package com.grcanosa.bots.bodabot

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, Props, Timers}
import com.bot4s.telegram.api.declarative.RegexCommands
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Message
import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}
import com.grcanosa.telegrambot.bot.BotWithAdmin
import com.grcanosa.telegrambot.bot.user.UserHandler
import com.grcanosa.telegrambot.dao.{BotDao, BotUserDao, InteractionDao}
import com.grcanosa.telegrambot.dao.mongo.{BotUserMongoDao, InteractionMongoDao}
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.model.BotUser.PERMISSION_ALLOWED
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

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
  with RegexCommands
{

  override val defaultUserPermission: BotUser.BotUserPermission = PERMISSION_ALLOWED

  val selfActor: ActorRef = system.actorOf(Props(new BodaBotActor()))

  object BodaBotUserActor{
    case object RemoveOldMessages
    case class CuandoMsg(msg: Message)
    case class DondeMsg(msg: Message)
    case class CuantoMsg(msg: Message)
    case class HolaMsg(msg: Message)
    case class SaeiMsg(msg: Message)
    case class QuienMsg(msg: Message)
    case class UnknownMsg(msg: Message)
  }
  import BodaBotUserActor._



  onRegex("(?i).*hola.*|.*ey.*|.*buenas.*|".r){ implicit msg =>
    groups => {
      allowedUser(Some("hola")){ uH =>
        uH.handler ! HolaMsg(msg)
      }
    }
  }

  onRegex("(?i).*sae+i+.*|.*marian.*".r){implicit msg =>
    groups => {
      allowedUser(Some("saei")){ uH =>
        uH.handler ! SaeiMsg(msg)
      }
    }
  }

  onRegex("(?i).*qui[eé]n.*".r){implicit msg =>
    groups => {
      allowedUser(Some("quien")){ uH =>
        uH.handler ! QuienMsg(msg)
      }
    }
  }

  onRegex("(?i).*cu[aá]ndo.*".r){implicit msg =>
    groups => {
      allowedUser(Some("cuando")){ uH =>
          uH.handler ! CuandoMsg(msg)
      }
    }
  }

  onRegex("(?i).*donde.*".r){implicit msg =>
    groups => {
      allowedUser(Some("donde")){ uH =>
       uH.handler ! DondeMsg(msg)
      }
    }
  }

  onRegex("(?i).*cu[aá]nto.*".r){implicit msg =>
    groups => {
      allowedUser(Some("cuanto")){ uH =>
        uH.handler ! CuantoMsg(msg)
      }
    }
  }

  onMessage{ implicit msg =>
    allowedUser(Some("msg")){uH =>
      uH.handler ! UnknownMsg(msg)
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




  class BodaBotUserActor(botUser: BotUser) extends Actor with Timers{
    var respondedMessages = Map.empty[Int,Long]
    def shouldRespond(msg: Message) = {
      if (!respondedMessages.contains(msg.messageId)) {
        respondedMessages = respondedMessages + (msg.messageId -> java.time.Instant.now().getEpochSecond)
        true
      }else{
        false
      }
    }
    def removeOldMessages() = {
      val n = java.time.Instant.now().getEpochSecond
      respondedMessages = respondedMessages.filter(d => (n - d._2 > 100))
    }
    timers.startTimerAtFixedRate("remove_old_messages_timer",RemoveOldMessages,1 minute)
    import BodaBotUserActor._
    override def receive = {
      case CuandoMsg(msg) if shouldRespond(msg) =>  reply(cuandoResponse)(msg)
      case DondeMsg(msg) if shouldRespond(msg) =>  reply(dondeResponse)(msg)
      case CuantoMsg(msg) if shouldRespond(msg) =>  reply(cuantoResponse(botUser.name))(msg)
      case HolaMsg(msg) if shouldRespond(msg) =>  reply(holaResponse(botUser.name))(msg)
      case SaeiMsg(msg) if shouldRespond(msg) =>  reply(saeiResponse(botUser.name))(msg)
      case UnknownMsg(msg) if shouldRespond(msg) => reply(unknownResponse(botUser.name))(msg)
      case QuienMsg(msg) if shouldRespond(msg) => reply(quienResponse(botUser.name))(msg)
      case RemoveOldMessages => removeOldMessages()
      case _ =>
    }
  }


  override def createNewUserActor(botUser: BotUser): ActorRef = {
    system.actorOf(Props(new BodaBotUserActor(botUser)),s"actor_${botUser.id}")
  }


}
