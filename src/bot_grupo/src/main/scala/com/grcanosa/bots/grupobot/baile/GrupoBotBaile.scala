package com.grcanosa.bots.grupobot.baile

import java.time.LocalDateTime

import akka.actor.{Actor, ActorRef, Props}
import com.bot4s.telegram.api.AkkaDefaults
import com.bot4s.telegram.api.declarative.Callbacks
import com.bot4s.telegram.methods.{EditMessageReplyMarkup, SendMessage}
import com.bot4s.telegram.models.{ChatId, Message}
import com.grcanosa.bots.grupobot.dao.{ConversationDao, WordCountDao}
import com.grcanosa.bots.grupobot.model.Conversation
import com.grcanosa.bots.grupobot.utils.GrupoUtils
import com.grcanosa.bots.grupobot.{GrupoBotData, GrupoBotHugChain, GrupoBotUserConversationRandomizer}
import com.grcanosa.telegrambot.bot.BotWithAdmin
import com.grcanosa.telegrambot.bot.BotWithAdmin.ForwardMessageTo
import com.grcanosa.telegrambot.dao.{BotDao, BotUserDao, InteractionDao}
import com.grcanosa.telegrambot.dao.mongo.{BotUserMongoDao, InteractionMongoDao}
import com.grcanosa.telegrambot.model.BotUser

import scala.util.Try

object GrupoBotBaile extends AkkaDefaults{

  val token = GrupoUtils.configBaileGrupo.getString("bot.token")
  val adminId = GrupoUtils.configBaileGrupo.getLong("bot.adminId")

  //  val redisHost = config.getString("redis.host")
  //  val redisPort = config.getInt("redis.port")
  //  val redisBaseKey = config.getString("bot.redis.keybase")
  //  val redisUserDao = new BotUserRedisDao(redisHost,redisPort,redisBaseKey)

  val mongoHost = GrupoUtils.configBaileGrupo.getString("mongo.host")
  val mongoPort = GrupoUtils.configBaileGrupo.getInt("mongo.port")
  val mongoDatabaseName = GrupoUtils.configBaileGrupo.getString("bot.mongo.databasename")

  implicit val ec = system.dispatcher

  val mongoUserDao = new BotUserMongoDao(mongoHost,mongoPort,mongoDatabaseName)

  val mongoInteractionDao = new InteractionMongoDao(mongoHost, mongoPort, mongoDatabaseName)

  implicit object GrupoDao extends BotDao{
    override def botUserDao: BotUserDao = mongoUserDao

    override def interactionDao: InteractionDao = mongoInteractionDao

  }

  val conversationDao = new ConversationDao(mongoHost,mongoPort,mongoDatabaseName)

  val wordCountDao = new WordCountDao(mongoHost,mongoPort,mongoDatabaseName)

  val grupoBot = new GrupoBotBaile(token,adminId,conversationDao,wordCountDao)
}

class GrupoBotBaile(override val token: String,
               override val adminId:Long,
               val conversationDao: ConversationDao,
               val wordCountDao: WordCountDao
              )
              (implicit botDao: BotDao)
extends BotWithAdmin(token, adminId)
with GrupoBotUserConversationRandomizer
//with GrupoBotHugChain
with Callbacks{

  import GrupoBotBaileData._

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
//
//  onCommand("/cadenadeabrazos"){ implicit msg =>
//    allowedUser(Some("cadenaabrazos")) { uH =>
//      val hugChain = newHugChain(uH)
//      val (txt,keyboard) = GrupoBotHugChain.getHugChainMessage(hugChain,permittedUserHandlers)
//      reply(txt,replyMarkup = keyboard)
//    }
//  }
//
//  onCallbackWithTag(hugChainCallbackDataKeyword){ implicit cbk =>
//    processHugChainCallbackData(cbk.message,cbk.data) match {
//      case Some((msg,newChain,destUH,txt,keyboard)) => {
//        val editM = EditMessageReplyMarkup(Some(ChatId(msg.source)), Some(msg.messageId), cbk.inlineMessageId, replyMarkup = None)
//        request(editM)
//        botActor ! SendMessage(destUH.user.id, txt, replyMarkup = keyboard)
//        keyboard match {
//          case Some(k) => botActor ! SendMessage(msg.source, chainContinuingText(newChain))
//          case None => {
//            val txtCompleted = chainCompletedText(newChain)
//            newChain
//              .users
//              .tail
//              .foreach { userH =>
//                botActor ! SendMessage(userH.user.id, txtCompleted)
//              }
//          }
//        }
//      }
//      case None => botlog.error("WTF!")
//    }
//  }
//
////

  val wordRegex = "[\\p{L}]+".r

  def addMessageToDao(message: Message) = {
    //BOTLOG.info(s"Adding message to DB ${message.text}")
    val words = Try {
      message.text.map { str =>
        wordRegex.findAllIn(str).matchData.map(_.group(0)).toList
      }
    }.recover{
      case e => botlog.error(s"$e"); Some(List.empty[String])
    }.getOrElse(Some(List.empty[String]))
    //BOTLOG.info(s"WORDS: $words")
    words.map(wordList =>
      wordList.map(_.toLowerCase).map(wordCountDao.increaseWordCount))
  }

  onMessage{ implicit msg =>
    isNotCommand { _ =>
    allowedUser(Some("message")) { uH =>
        addMessageToDao(msg)
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
        botlog.info(s"Cancelling conversation between ${conv.uh1.user.name} and ${conv.uh2.user.name}")
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
