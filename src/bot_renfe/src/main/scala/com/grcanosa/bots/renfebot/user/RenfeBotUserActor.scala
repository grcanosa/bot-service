package com.grcanosa.bots.renfebot.user

import akka.actor.{Actor, ActorRef}
import com.bot4s.telegram.models.Message
import com.grcanosa.bots.renfebot.model.JourneyCheck
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.utils.LazyBotLogging



object RenfeBotUserActor{

  sealed abstract trait RenfeBotUserMsg

  case object MenuCommand extends RenfeBotUserMsg
  case object CancelCommand extends RenfeBotUserMsg
  case class KeyboardCallbackData(messageId: Int,data: String) extends RenfeBotUserMsg

}

class RenfeBotUserActor(val botUser: BotUser,val botActor: ActorRef)
  extends Actor
with LazyBotLogging{

  import RenfeBotUserActor._

  var renfeBotUser: RenfeBotUser = RenfeBotUser(botUser)

  override def receive = {
    case MenuCommand => {
      botlog.info(s"Received menu from user ${botUser.name}-${botUser.id}")
      val resp = renfeBotUser.menuMessage()
      renfeBotUser = resp.renfeBotUser
      resp.responses.foreach(botActor ! _)

    }
    case CancelCommand => {
      botlog.info(s"Received cancel from user ${botUser.name}-${botUser.id}")
      val resp = renfeBotUser.cancelMessage()
      renfeBotUser = resp.renfeBotUser
      resp.responses.foreach(botActor ! _)
    }

    case msg: Message => {
      val resp = renfeBotUser.processMessage(msg)
      renfeBotUser = resp.renfeBotUser
      resp.responses.foreach { msg =>
        botActor ! msg
        Thread.sleep(10)
      }
    }

    case KeyboardCallbackData(messageId,data) => {
      val resp = renfeBotUser.processKeyboardCallbackData(messageId,data)
      renfeBotUser = resp.renfeBotUser
      resp.responses.foreach(botActor ! _)
    }

    case JourneyCheck(journey,results) => {

    }
  }

}
