 package com.grcanosa.bots.renfebot

 import java.time.LocalDate
 import java.time.format.DateTimeFormatter

 import akka.actor.{Actor, ActorRef, Props}
 import com.bot4s.telegram.api.AkkaDefaults
 import com.bot4s.telegram.api.declarative.Callbacks
 import com.grcanosa.bots.renfebot.RenfeBot.{AddTripToDao, CleanDao, RemoveTripFromDao}
 import com.grcanosa.bots.renfebot.dao.TripsDao
 import com.grcanosa.bots.renfebot.model.Trip
 import com.grcanosa.telegrambot.bot.BotWithAdmin
 import com.grcanosa.telegrambot.dao.{BotDao, BotUserDao, InteractionDao}
 import com.grcanosa.telegrambot.dao.mongo.{BotUserMongoDao, InteractionMongoDao}
 import com.grcanosa.telegrambot.model.BotUser
 import com.grcanosa.telegrambot.utils.CalendarKeyboard
 import com.typesafe.config.ConfigFactory

 import scala.concurrent.duration._
 import scala.util.{Failure, Success}

 object RenfeBot extends AkkaDefaults{

   lazy val config = ConfigFactory.load("renfebot.conf")

   lazy val token = config.getString("bot.token")
   lazy val adminId = config.getLong("bot.adminId")

   lazy val mongoHost = config.getString("mongo.host")
   lazy val mongoPort = config.getInt("mongo.port")
   lazy val mongoDatabaseName = config.getString("bot.mongo.databasename")

   implicit val ec = system.dispatcher

   val mongoUserDao = new BotUserMongoDao(mongoHost,mongoPort,mongoDatabaseName)

   val mongoInteractionDao = new InteractionMongoDao(mongoHost, mongoPort, mongoDatabaseName)

   implicit val tripDao = new TripsDao(mongoHost,mongoPort,mongoDatabaseName)

   implicit object RenfeDao extends BotDao{
     override def botUserDao: BotUserDao = mongoUserDao

     override def interactionDao: InteractionDao = mongoInteractionDao
   }


   val bot = new RenfeBot(token,adminId)

   case class AddTripToDao(user: BotUser, trip: Trip)
   case class RemoveTripFromDao(user: BotUser, trip: Trip)
   case object CleanDao

 }


 class RenfeBot(override val token: String
                   , override val adminId: Long
                   )
                  (implicit botDao: BotDao, tripsDao: TripsDao)
   extends BotWithAdmin(token,adminId)
 with Callbacks
 with CalendarKeyboard{

   import RenfeBotUserActor._

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
    cbk.message.foreach{ implicit msg =>
      allowedUser(Some("keyboard_callback")){ uH =>
        cbk.data.foreach{ data =>
          uH.handler ! KeyboardCallbackData(data)
        }
      }
    }
   }


   system.scheduler.schedule(0 seconds, 12 hours){
     botActor ! CleanDao
   }

   val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

   override def additionalReceive: ActorReceive = {
     case AddTripToDao(user, trip) => {
      tripsDao.addTrip(user,trip).onComplete{
        case Success(true) =>
        case Success(false) =>
        case Failure(excp) =>
      }
     }
     case RemoveTripFromDao(user, trip) => {
      tripsDao.removeTrip(user,trip).onComplete{
        case Success() =>
        case Failure() =>
      }
     }
     case CleanDao => {
       val lastDate = LocalDate.now().format(dateFormatter)
       tripsDao.markOldTripsAsInactive(lastDate).onComplete{
         case Success()
         case Failure()
       }
     }
     case _ =>
   }


   override def createNewUserActor(botUser: BotUser): ActorRef = {
     system.actorOf(Props(new RenfeBotUserActor(botUser,botActor)),s"actor_${botUser.id}")
   }

   override def userNotAllowedResponse(name: String): String = notAllowedText

   override def userRequestPermissionResponse(name: String) = requestingPermissionText

   override def startCmdResponse(name: String): String = startText

   override def helpCmdResponse(name: String) = helpText


 }
