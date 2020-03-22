package com.grcanosa.bots.renfebot.user

import akka.actor.{Actor, ActorRef}
import com.bot4s.telegram.models.Message
import com.grcanosa.telegrambot.model.BotUser



object RenfeBotUserActor{

  sealed abstract trait RenfeBotUserMsg

  case object MenuCommand extends RenfeBotUserMsg
  case object CancelCommand extends RenfeBotUserMsg
  case class KeyboardCallbackData(messageId: Int,data: String) extends RenfeBotUserMsg

}

class RenfeBotUserActor(val botUser: BotUser,val botActor: ActorRef) extends Actor {

  import RenfeBotUserActor._

  var renfeBotUser: RenfeBotUser = RenfeBotUser(botUser)

  override def receive = {
    case MenuCommand => {
      renfeBotUser = renfeBotUser.menuMessage()
      renfeBotUser.responses.foreach(botActor ! _)

    }
    case CancelCommand => {
      renfeBotUser = renfeBotUser.cancelMessage()
      renfeBotUser.responses.foreach(botActor ! _)
    }

    case msg: Message => {
      renfeBotUser = renfeBotUser.processMessage(msg)
      renfeBotUser.responses.foreach(botActor ! _)
    }

    case KeyboardCallbackData(messageId,data) => {
      renfeBotUser = renfeBotUser.processKeyboardCallbackData(messageId,data)
      renfeBotUser.responses.foreach(botActor ! _)
    }
  }

}
