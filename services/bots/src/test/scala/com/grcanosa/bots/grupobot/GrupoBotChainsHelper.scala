package com.grcanosa.bots.grupobot

import akka.actor.ActorRef
import com.grcanosa.bots.grupobot.GrupoBotHugChain.HugChain
import com.grcanosa.telegrambot.bot.user.UserHandler
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.model.BotUser.PERMISSION_ALLOWED

trait GrupoBotChainsHelper {
  val permittedUsers = (1 to 10).map{ n =>
    UserHandler(BotUser(n,PERMISSION_ALLOWED, s"user$n",None,None), ActorRef.noSender)
  }

  val emptyChain: HugChain = HugChain("1",Nil)

  val chainWithAllUsers = HugChain("2",permittedUsers.toList)
}
