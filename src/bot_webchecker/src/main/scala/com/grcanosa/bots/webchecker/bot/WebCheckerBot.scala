package com.grcanosa.bots.webchecker.bot

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.{ActorRef, Props}
import com.bot4s.telegram.api.AkkaDefaults
import com.bot4s.telegram.api.declarative.Callbacks
import com.bot4s.telegram.methods.SendMessage
import com.grcanosa.bots.renfebot.bot.RenfeBot.{AddTripToDao, CheckTripForUsers, CleanDao, RemoveTripFromDao}
import com.grcanosa.bots.renfebot.dao.TripsDao
import com.grcanosa.bots.renfebot.model.Journey
import com.grcanosa.bots.renfebot.renfe.RenfeCheckerActor
import com.grcanosa.bots.renfebot.renfe.RenfeCheckerActor.CheckJourney
import com.grcanosa.bots.renfebot.user.RenfeBotUserActor
import com.grcanosa.telegrambot.bot.BotWithAdmin
import com.grcanosa.telegrambot.dao.mongo.{BotUserMongoDao, InteractionMongoDao}
import com.grcanosa.telegrambot.dao.{BotDao, BotUserDao, InteractionDao}
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.utils.{CalendarKeyboard, LazyBotLogging}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object WebCheckerBot extends AkkaDefaults{
  lazy val config: Config = ConfigFactory.load("webchecker.conf")

  lazy val token: String = config.getString("bot.token")
  lazy val adminId: Long = config.getLong("bot.adminId")

  lazy val mongoHost: String = config.getString("mongo.host")
  lazy val mongoPort: Int = config.getInt("mongo.port")
  lazy val mongoDatabaseName: String = config.getString("bot.mongo.databasename")

  lazy val driverUrl: String = config.getString("bot.renfe.seleniumDriverUrl")

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val mongoUserDao: BotUserMongoDao = new BotUserMongoDao(mongoHost,mongoPort,mongoDatabaseName)

  val mongoInteractionDao: InteractionMongoDao = new InteractionMongoDao(mongoHost, mongoPort, mongoDatabaseName)


  implicit object WebCheckerDao extends BotDao{
    override def botUserDao: BotUserDao = mongoUserDao

    override def interactionDao: InteractionDao = mongoInteractionDao
  }


  val bot = new WebCheckerBot(token,adminId,driverUrl)


}

class WebCheckerBot(override val token: String
, override val adminId: Long
, val driverUrl: String
)
(implicit botDao: BotDao)
  extends BotWithAdmin(token,adminId)
    with Callbacks
  with LazyBotLogging
{



  override def createNewUserActor(botUser: BotUser): ActorRef = {
    botlog.info(s"Creating actor for user: $botUser with botActor $botActor")
    system.actorOf(Props(new WebCheckerUserActor(botUser,botActor)),s"actor_${botUser.id}")
  }
}
