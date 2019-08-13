package com.grcanosa.bots.grupobot

import akka.actor.ActorRef
import com.grcanosa.telegrambot.bot.user.UserHandler
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.model.BotUser.PERMISSION_ALLOWED
import org.scalatest.{Matchers, WordSpec}

class GrupoBotDataSpec extends WordSpec with Matchers{

  "GrupoBotData " should {

    "perform a correct chain final message" in {
      val list = (1 to 6).map(n => UserHandler(BotUser(n.toLong,PERMISSION_ALLOWED,"user_"+n.toString,None,None),ActorRef.noSender)).toList

      val txt = GrupoBotData.abrazoRecursion(list,"")

      println(txt)
      for{
        u <- list.map(_.user.name)
      } yield txt.replace(".","").replace(",","").split(" ") should contain (u)


    }

  }

}
