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
import com.grcanosa.telegrambot.model.BotUser.{PERMISSION_ALLOWED, PERMISSION_NOT_SET}
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.matching.Regex

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

  override val defaultUserPermission: BotUser.BotUserPermission = PERMISSION_NOT_SET

  val selfActor: ActorRef = system.actorOf(Props(new BodaBotActor()))

  object BodaBotUserActor{
    case object RemoveOldMessages
    case class RespondMsg(r: String=> String,msg:Message)
    case class UnknownMsg(msg: Message)

    val regexInteractionsResponsesMatches: Seq[(Regex, String, String => String)] = Seq(
      ("(?i)\\bhola\\b|\\bey\\b|\\bbuenas\\b".r,"hola",holaResponse)
      ,("(?i).*sae+i+.*|\\bmarian\\b".r,"saei",saeiResponse)
      ,("(?i).*qui[eé]n.*".r,"quien",quienResponse)
      ,("(?i).*cu[aá]ndo.*".r,"cuando",cuandoResponse)
      ,("(?i).*donde.*".r,"donde",dondeResponse)
      ,("(?i)\\bMer(?:cedes)?\\b|\\bIsa(?:bel)?\\b".r,"novias",queVivanLasNoviasResponse)
      ,("(?i).*cu[aá]nto.*".r,"cuanto",cuantoResponse)
    )
  }
  import BodaBotUserActor._


  regexInteractionsResponsesMatches.foreach{ case (r,i,resp) =>
    onRegex(r){implicit msg =>
      groups => {
        allowedUser(Some(i)){ uH =>
          uH.handler ! RespondMsg(resp,msg)
        }
      }
    }
  }


  val startHelpRegex = "\\/start|\\/help".r

  onMessage{ implicit msg =>
    allowedUser(Some("msg")){uH =>
      if(msg.text.isDefined && startHelpRegex.findFirstMatchIn(msg.text.get).isDefined){
        //Ignoring message
      }else{
        uH.handler ! UnknownMsg(msg)
      }
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
      case RespondMsg(resp,msg) if shouldRespond(msg) => reply(resp(botUser.name))(msg)
      case UnknownMsg(msg) if shouldRespond(msg) => reply(unknownResponse(botUser.name))(msg)
      case RemoveOldMessages => removeOldMessages()
      case _ =>
    }
  }


  override def createNewUserActor(botUser: BotUser): ActorRef = {
    system.actorOf(Props(new BodaBotUserActor(botUser)),s"actor_${botUser.id}")
  }


}
