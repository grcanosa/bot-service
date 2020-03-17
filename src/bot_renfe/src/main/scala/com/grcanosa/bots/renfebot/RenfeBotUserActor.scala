package com.grcanosa.bots.renfebot

import akka.actor.{Actor, ActorRef}
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Message
import com.grcanosa.bots.renfebot.RenfeBotUserActor.{CancelCommand, MenuCommand, START_STATE}
import com.grcanosa.bots.renfebot.user.RenfeBotUser
import com.grcanosa.bots.renfebot.user.RenfeBotUser.START_STATE
import com.grcanosa.telegrambot.model.BotUser
import io.circe.Decoder.state


object RenfeBotUserActor{

  sealed abstract trait RenfeBotUserMsg

  case object MenuCommand extends RenfeBotUserMsg
  case object CancelCommand extends RenfeBotUserMsg

}

class RenfeBotUserActor(val botUser: BotUser,val botActor: ActorRef) extends Actor {

  import RenfeBotUserActor._

  var renfeBotUser: RenfeBotUser = RenfeBotUser(botUser)

  import RenfeBotData._

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
  }

}
