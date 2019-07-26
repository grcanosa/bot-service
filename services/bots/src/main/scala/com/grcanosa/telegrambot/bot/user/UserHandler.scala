package com.grcanosa.telegrambot.bot.user

import akka.actor.ActorRef
import com.grcanosa.telegrambot.model.BotUser

case class UserHandler(user: BotUser, handler: ActorRef)
