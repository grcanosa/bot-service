package com.grcanosa.bots.grupobot

import akka.actor.ActorRef
import com.grcanosa.telegrambot.bot.user.UserHandler
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.model.BotUser.PERMISSION_ALLOWED
import org.scalatest.{Matchers, WordSpec}

class GrupoBotHugChainSpec extends WordSpec with Matchers with GrupoBotChainsHelper {

  import GrupoBotHugChain._

  "increaseChain method " should {

    "return a new chain with one user if user seq is empty" in {
      increaseChain(permittedUsers(0),emptyChain).users shouldBe List(permittedUsers(0))
    }
    "return a new chain with the provided user in front" in {
      val hc0 = increaseChain(permittedUsers(0),emptyChain)
      increaseChain(permittedUsers(1),hc0).users shouldBe List(permittedUsers(1),permittedUsers(0))
    }
  }

  "getRemainingUserHandlers method " should {
    "provide all users for chain with no users" in {
      getRemainingUserHandlers(emptyChain,permittedUsers) should contain allElementsOf (permittedUsers)
    }
    "provide all but one users for chain with one users" in {
      val chainWithOneUser = increaseChain(permittedUsers(0),emptyChain)
      val remain = getRemainingUserHandlers(chainWithOneUser,permittedUsers)
      remain should not contain permittedUsers(0)
      remain should contain allElementsOf permittedUsers.filter(_.user.id != permittedUsers(0).user.id)
    }

    "provide only one user for chain with all but one users" in {
      val chainWithAllButFirstUser = permittedUsers.tail.foldLeft(emptyChain){case (c,u) => increaseChain(u,c)}
      val remain = getRemainingUserHandlers(chainWithAllButFirstUser,permittedUsers)
      remain shouldBe List(permittedUsers.head)
    }

    "provide no users for chain with all users" in {
      getRemainingUserHandlers(chainWithAllUsers,permittedUsers) shouldBe List()
    }
  }

  "chainStartedMessage method" should {
    "provide a text with starting user and a keyboard with all but one users" in {
      val ch = increaseChain(permittedUsers(0),emptyChain)
      val (txt,keyboard) = chainStartedMessage(ch,permittedUsers)
      txt.contains(permittedUsers(0).user.name) shouldBe true
      keyboard shouldBe defined
      keyboard.get.inlineKeyboard.flatten.size shouldBe permittedUsers.tail.size
      keyboard.get.inlineKeyboard.flatten.map(_.text) should not contain (permittedUsers(0).user.name)
    }
  }

  "chainContinueMessage method" should {

    "provide correct message and keyboard" in {
      val ch = increaseChain(permittedUsers(0), emptyChain)
      val ch2 = increaseChain(permittedUsers(1), ch)
      val (_, keyboard) = chainContinueMessage(ch2, permittedUsers)
      keyboard.get.inlineKeyboard.flatten.size shouldBe permittedUsers.size - 2
      val ch3 = increaseChain(permittedUsers(2), ch2)
      val (_, keyboard2) = chainContinueMessage(ch3, permittedUsers)
      keyboard2.get.inlineKeyboard.flatten.size shouldBe permittedUsers.size - 3

    }
  }

  "chainEndedMessage method" should {
    "provide correct message and keyboard" in {
      val (txt,keyboard) = chainEndedMessage(chainWithAllUsers)
      keyboard shouldBe None
    }
  }

  "getHugChainMessage method " should {

    "provide no message and no keyboard if emptyChain" in {
      getHugChainMessage(emptyChain,permittedUsers) shouldBe ("",None)
    }

    "provide chainStartedMessage if chain with one user" in {
      val ch = increaseChain(permittedUsers(0),emptyChain)
      val (_,keyboard) = getHugChainMessage(ch,permittedUsers)
      val (_,keyboard2) = chainStartedMessage(ch,permittedUsers)
      keyboard shouldBe keyboard2
    }

    "provide chainEndedMessage for chain with all users" in {
      val (_,keyboard) = getHugChainMessage(chainWithAllUsers,permittedUsers)
      val (_,keyboard2) = chainEndedMessage(chainWithAllUsers)
      keyboard shouldBe keyboard2
    }

    "provide chainContinueMessage for chain with more than one users and less than total" in {
      val ch = increaseChain(permittedUsers(0),emptyChain)
      val ch2 = increaseChain(permittedUsers(1),ch)

      val (_,keyboard) = getHugChainMessage(ch2,permittedUsers)
      val (_,keyboard2) = chainContinueMessage(ch2,permittedUsers)
      keyboard shouldBe keyboard2
    }

  }

}
