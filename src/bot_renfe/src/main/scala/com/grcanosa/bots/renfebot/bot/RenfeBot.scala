 package com.grcanosa.bots.renfebot.bot

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

 object RenfeBot extends AkkaDefaults{

   lazy val config: Config = ConfigFactory.load("renfebot.conf")

   lazy val token: String = config.getString("bot.token")
   lazy val adminId: Long = config.getLong("bot.adminId")

   lazy val mongoHost: String = config.getString("mongo.host")
   lazy val mongoPort: Int = config.getInt("mongo.port")
   lazy val mongoDatabaseName: String = config.getString("bot.mongo.databasename")

   lazy val driverUrl: String = config.getString("bot.renfe.seleniumDriverUrl")

   implicit val ec: ExecutionContextExecutor = system.dispatcher

   val mongoUserDao: BotUserMongoDao = new BotUserMongoDao(mongoHost,mongoPort,mongoDatabaseName)

   val mongoInteractionDao: InteractionMongoDao = new InteractionMongoDao(mongoHost, mongoPort, mongoDatabaseName)

   implicit val tripDao: TripsDao = new TripsDao(mongoHost,mongoPort,mongoDatabaseName)

   implicit object RenfeDao extends BotDao{
     override def botUserDao: BotUserDao = mongoUserDao

     override def interactionDao: InteractionDao = mongoInteractionDao
   }


   val bot = new RenfeBot(token,adminId,driverUrl)

   case class AddTripToDao(user: BotUser, trip: Journey)
   case class RemoveTripFromDao(user: BotUser, trip: Journey)
   case object CleanDao
   case class CheckTripForUsers(journey: Journey, users: Seq[Long])

 }


 class RenfeBot(override val token: String
                   , override val adminId: Long
               , val driverUrl: String
                   )
                  (implicit botDao: BotDao, tripsDao: TripsDao)
   extends BotWithAdmin(token,adminId)
 with Callbacks
 with CalendarKeyboard
 with LazyBotLogging{

   import RenfeBotData._
   import RenfeBotUserActor._

   botlog.info("Created Bot")

   val renfeCheckerActor: ActorRef = system.actorOf(Props(new RenfeCheckerActor(driverUrl)),s"actor_renfechecker")

   onCommand("/menu"){ implicit msg =>
     allowedUser(Some("menu")){ uH =>
       uH.handler ! MenuCommand
     }
   }

   onCommand("/cancel") { implicit msg =>
       allowedUser(Some("cancel")) { uH =>
         uH.handler ! CancelCommand
       }
   }

   onMessage{ implicit msg =>
     isNotCommand{ _ =>
      allowedUser(Some("message")) { uH =>
        uH.handler ! msg
      }
     }
   }


   onCallbackWithTag(KEYBOARD_TAG){ implicit cbk =>
     botlog.info(s"Callback with tag: $KEYBOARD_TAG, data: ${cbk.data}")
    cbk.message.foreach{ implicit msg =>
      getUser(msg.chat.id).foreach{ uH =>
        cbk.data.foreach{ data =>
          uH.handler ! KeyboardCallbackData(msg.messageId,data)
        }
      }
    }
   }


   system.scheduler.schedule(30 seconds, 24 hours){
     botActor ! CleanDao
   }

   val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

   override def additionalReceive: ActorReceive = {
     case AddTripToDao(user, trip) => {
      tripsDao.addTrip(user,trip).onComplete{
        case Success(true) => botActor ! SendMessage(user.id,tripAddedText,replyMarkup = Some(removeKeyboard))
        case Success(false) =>  botActor ! SendMessage(user.id,errorAddingTripText,replyMarkup = Some(removeKeyboard))
        case Failure(excp) =>  botActor ! SendMessage(user.id,errorAddingTripText,replyMarkup = Some(removeKeyboard))
      }
     }
     case RemoveTripFromDao(user, trip) => {
      tripsDao.removeTrip(user,trip).onComplete{
        case Success(_) => botActor ! SendMessage(user.id,tripRemovedText,replyMarkup = Some(removeKeyboard))
        case Failure(_) => botActor ! SendMessage(user.id,tripRemovedErrorText,replyMarkup = Some(removeKeyboard))
      }
     }
     case CleanDao => {
       val lastDate = LocalDate.now().format(dateFormatter)
       tripsDao.markOldTripsAsInactive(lastDate).onComplete{
         case Success(_) => botActor ! SendMessage(adminId,tripDaoCleanText,replyMarkup = Some(removeKeyboard))
         case Failure(_) => botActor ! SendMessage(adminId,tripDaoCleanErrorText,replyMarkup = Some(removeKeyboard))
       }
     }
     case check: CheckTripForUsers => {
       val userHandlers = check.users.flatMap{ id =>
         getUser(id).map(_.handler)
       }
       renfeCheckerActor ! CheckJourney(check.journey,userHandlers)
     }
     case _ =>
   }


   override def createNewUserActor(botUser: BotUser): ActorRef = {
     botlog.info(s"Creating actor for user: $botUser with botActor $botActor")
     system.actorOf(Props(new RenfeBotUserActor(botUser,botActor)),s"actor_${botUser.id}")
   }

   override def userNotAllowedResponse(name: String): String = notAllowedText(name)

   override def userRequestPermissionResponse(name: String) = requestingPermissionText(name)

   override def startCmdResponse(name: String): String = startText(name)

   override def helpCmdResponse(name: String) = helpText(name)


 }
