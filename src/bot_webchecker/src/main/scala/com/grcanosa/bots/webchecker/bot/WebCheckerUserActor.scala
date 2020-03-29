package com.grcanosa.bots.webchecker.bot

import akka.actor.{Actor, ActorRef}
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.utils.LazyBotLogging

class WebCheckerUserActor(val botUser: BotUser, val botActor: ActorRef) extends Actor with LazyBotLogging{

  override def receive = {
    case _ => botlog.info("Message received")
  }

}
