package com.grcanosa.bots.grupobot.model

import akka.actor.Cancellable
import com.grcanosa.telegrambot.bot.user.UserHandler

case class Conversation(uh1: UserHandler
                        , uh2: UserHandler
                        , cancel: Option[Cancellable]
                        , start: String
                       , end: String)
