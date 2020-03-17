 package com.grcanosa.bots.renfebot

 import akka.actor.{Actor, ActorRef, Props}
 import com.bot4s.telegram.api.AkkaDefaults
 import com.bot4s.telegram.models.Message
 import com.grcanosa.telegrambot.bot.BotWithAdmin
 import com.grcanosa.telegrambot.dao.{BotDao, BotUserDao, InteractionDao}
 import com.grcanosa.telegrambot.dao.mongo.{BotUserMongoDao, InteractionMongoDao}
 import com.grcanosa.telegrambot.model.BotUser
 import com.typesafe.config.ConfigFactory
 import com.typesafe.sslconfig.util.ConfigLoader

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

   implicit object RenfeDao extends BotDao{
     override def botUserDao: BotUserDao = mongoUserDao

     override def interactionDao: InteractionDao = mongoInteractionDao
   }


   val bot = new RenfeBot(token,adminId)

 }


 class RenfeBot(override val token: String
                   , override val adminId: Long
                   )
                  (implicit botDao: BotDao)
   extends BotWithAdmin(token,adminId){

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


   class RenfeBotActor extends Actor{
     override def receive = {
       case _ => println("hola")
     }
   }



   override def createNewUserActor(botUser: BotUser): ActorRef = {
     system.actorOf(Props(new RenfeBotActor),s"actor_${botUser.id}")
   }

   override def userNotAllowedResponse(name: String): String = notAllowedText

   override def userRequestPermissionResponse(name: String) = requestingPermissionText

   override def startCmdResponse(name: String): String = startText

   override def helpCmdResponse(name: String) = helpText


 }
