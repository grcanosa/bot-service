package com.grcanosa.bots.grupobot

import akka.actor.ActorRef
import com.grcanosa.telegrambot.bot.user.UserHandler
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.model.BotUser.PERMISSION_ALLOWED
import org.scalatest.{Matchers, WordSpec}

class GrupoBotDataSpec extends WordSpec with Matchers with GrupoBotChainsHelper {

  import GrupoBotHugChain._
  import GrupoBotData._

  "GrupoBotData " should {

    "perform a correct chain final message" in {
      val list = (1 to 6).map(n => UserHandler(BotUser(n.toLong,PERMISSION_ALLOWED,"user_"+n.toString,None,None),ActorRef.noSender)).toList

      val txt = GrupoBotData.abrazoRecursion(list,"")

      info(txt)
      for{
        u <- list.map(_.user.name)
      } yield txt.replace(".","").replace(",","").split(" ") should contain (u)
    }

 }

  "chainContinueMessage" should {

    "start with correct combination in chain with 2 users" in {
      val ch = increaseChain(permittedUsers(0),emptyChain)
      val ch2 = increaseChain(permittedUsers(1),ch)
      val txt = hugChainContinueText(ch2)
      val name0 = permittedUsers(0).user.name
      val name1 = permittedUsers(1).user.name
      txt should startWith (s"$name1, $name0".bottext)
    }

    "start with correct combination in chain with 4 users" in {
      val ch = permittedUsers.slice(0,4).foldLeft(emptyChain)((ch,u) => increaseChain(u,ch))
      val txt = hugChainContinueText(ch)
      val name0 = permittedUsers(2).user.name
      val name1 = permittedUsers(3).user.name
      txt should startWith (s"$name1, $name0".bottext)
    }

  }

  "chainCompletedText" should {
    "behave correctly for expected chain" in {
      val ch = permittedUsers.foldLeft(emptyChain)((ch,u) => increaseChain(u,ch))
      val txt = chainCompletedText(ch)
      info(txt)
      for {
        u <- permittedUsers.map(_.user.name)
      } yield txt should include (u)
    }
  }

  "hugChainEndText" should {
    "behave correctly for expected chain" in {
      val ch = permittedUsers.foldLeft(emptyChain)((ch,u) => increaseChain(u,ch))
      val txt = hugChainEndText(ch)
      info(txt)
      for {
        u <- permittedUsers.map(_.user.name)
      } yield txt should include (u)
    }
  }



}
