// package com.grcanosa.bots.renfebot

// import akka.actor.{Actor, ActorRef, Props}
// import com.bot4s.telegram.api.AkkaDefaults
// import com.grcanosa.bots.grcanosabot.GrcanosaBotData.{helpText, notAllowedText, requestingPermissionText, startText}
// import com.grcanosa.telegrambot.bot.BotWithAdmin
// import com.grcanosa.telegrambot.dao.{BotDao, BotUserDao, InteractionDao}
// import com.grcanosa.telegrambot.dao.mongo.{BotUserMongoDao, InteractionMongoDao}
// import com.grcanosa.telegrambot.model.BotUser

// object RenfeBot extends AkkaDefaults{

//   import RenfeBotUtils._

//   val token = configRenfe.getString("bot.token")
//   val adminId = configRenfe.getLong("bot.adminId")

//   val mongoHost = configRenfe.getString("mongo.host")
//   val mongoPort = configRenfe.getInt("mongo.port")
//   val mongoDatabaseName = configRenfe.getString("bot.mongo.databasename")

//   implicit val ec = system.dispatcher

//   val mongoUserDao = new BotUserMongoDao(mongoHost,mongoPort,mongoDatabaseName)

//   val mongoInteractionDao = new InteractionMongoDao(mongoHost, mongoPort, mongoDatabaseName)

//   implicit object RenfeDao extends BotDao{
//     override def botUserDao: BotUserDao = mongoUserDao

//     override def interactionDao: InteractionDao = mongoInteractionDao
//   }


//   val bot = new RenfeBot(token,adminId)

// }


// class RenfeBot(override val token: String
//                   , override val adminId: Long
//                   )
//                  (implicit botDao: BotDao)
//   extends BotWithAdmin(token,adminId){


//   class RenfeBotActor extends Actor{
//     override def receive = {
//       case _ => println("hola")
//     }
//   }



//   override def createNewUserActor(botUser: BotUser): ActorRef = {
//     system.actorOf(Props(new RenfeBotActor),s"actor_${botUser.id}")
//   }

//   override def userNotAllowedResponse(name: String): String = notAllowedText

//   override def userRequestPermissionResponse(name: String) = requestingPermissionText

//   override def startCmdResponse(name: String): String = startText

//   override def helpCmdResponse(name: String) = helpText


// }