package com.grcanosa.grupobot

import java.time.LocalDateTime

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import com.bot4s.telegram.api.AkkaDefaults
import com.bot4s.telegram.methods.{ForwardMessage, SendMessage}
import com.grcanosa.grupobot.dao.ConversationDao
import com.grcanosa.grupobot.model.Conversation
import com.grcanosa.telegrambot.bot.BotWithAdmin
import com.grcanosa.telegrambot.bot.BotWithAdmin.ForwardMessageTo
import com.grcanosa.telegrambot.bot.user.UserHandler
import com.grcanosa.telegrambot.dao.{BotDao, BotUserDao, InteractionDao}
import com.grcanosa.telegrambot.dao.mongo.{BotUserMongoDao, InteractionMongoDao}
import com.grcanosa.telegrambot.dao.redis.BotUserRedisDao
import com.grcanosa.telegrambot.model.BotUser

import scala.concurrent.duration._

object GrupoBot extends AkkaDefaults {
  import com.grcanosa.grupobot.utils.GrupoUtils._

  val token = configGrupo.getString("bot.token")
  val adminId = configGrupo.getLong("bot.adminId")

//  val redisHost = config.getString("redis.host")
//  val redisPort = config.getInt("redis.port")
//  val redisBaseKey = config.getString("bot.redis.keybase")
//  val redisUserDao = new BotUserRedisDao(redisHost,redisPort,redisBaseKey)

  val mongoHost = configGrupo.getString("mongo.host")
  val mongoPort = configGrupo.getInt("mongo.port")
  val mongoDatabaseName = configGrupo.getString("bot.mongo.databasename")

  implicit val ec = system.dispatcher

  val mongoUserDao = new BotUserMongoDao(mongoHost,mongoPort,mongoDatabaseName)

  val mongoInteractionDao = new InteractionMongoDao(mongoHost, mongoPort, mongoDatabaseName)

  implicit object GrupoDao extends BotDao{
    override def botUserDao: BotUserDao = mongoUserDao

    override def interactionDao: InteractionDao = mongoInteractionDao

  }

  val conversationDao = new ConversationDao(mongoHost,mongoPort,mongoDatabaseName)

  val grupoBot = new GrupoBot(token,adminId,conversationDao)


}





class GrupoBot(override val token: String
               , override val adminId:Long,
               val conversationDao: ConversationDao)
              (implicit botDao: BotDao)
extends BotWithAdmin(token, adminId)
with GrupoBotUserConversationRandomizer{

  import GrupoBotData._
  import com.grcanosa.telegrambot.utils.BotUtils._

  val grupoBotActor = system.actorOf(Props(new GrupoBotActor()))

  onCommand("/cancelconexion") { implicit msg =>
    allowedUser(Some("cancelconexion")) { uH =>
      getConversationForUser(uH) match {
        case Some(conv) => {
          conv.cancel.foreach(_.cancel())
          grupoBotActor ! CancelConversation(conv)
        }
        case None => {
          botActor ! SendMessage(uH.user.id,noConversationAssigned(uH.user.name))
        }
      }
    }
  }

  onMessage{ implicit msg =>
    allowedUser(Some("message")) { uH =>
      isNotCommand { _ =>
        getUserConversation(uH) match {
          case Some(conv) => {
            botActor ! ForwardMessageTo(getConversationDestination(uH, conv).user.id, msg)
            conv.cancel.foreach(_.cancel())
            val cancellable = system.scheduler.scheduleOnce(conversationDuration) {
              grupoBotActor ! CancelConversation(conv)
            }
            userConversations = conv.copy(cancel = Some(cancellable)) +: userConversations.filter(c1 => c1 isNotEqual conv)
          }
          case None => botActor ! SendMessage(uH.user.id, noConversationReadyText(uH.user.name))
        }
      }
    }
  }



  class GrupoBotActor extends Actor{

    def receive = {
      case CancelConversation(conv) => {
        BOTLOG.info(s"Cancelling conversation between ${conv.uh1.user.name} and ${conv.uh2.user.name}")
        userConversations = userConversations.filter(_ isNotEqual conv)
        botActor ! SendMessage(conv.uh1.user.id,conversationEndedText)
        botActor ! SendMessage(conv.uh2.user.id,conversationEndedText)
        conversationDao.addConversation(conv.copy(end=LocalDateTime.now().toString))
      }

      case _ =>
    }
  }

  class GrupoBotUserActor extends Actor{
    override def receive = {
      case _ =>
    }
  }

  override def createNewUserActor(botUser: BotUser): ActorRef = {
    system.actorOf(Props(new GrupoBotUserActor()),s"useractor_${botUser.id}")
  }

  override def userNotAllowedResponse(name: String): String = notAllowedText

  override def userRequestPermissionResponse(name: String) = requestingPermissionText

  override def startCmdResponse(name: String): String = startText

  override def helpCmdResponse(name: String) = helpText

  override def newConversation(conv: Conversation): Unit = {
    botActor ! SendMessage(conv.uh1.user.id,newConversationText(conv.uh1.user.name))
    botActor ! SendMessage(conv.uh2.user.id,newConversationText(conv.uh2.user.name))
  }

  override def permissionGrantedResponse = permissionGrantedText
}
