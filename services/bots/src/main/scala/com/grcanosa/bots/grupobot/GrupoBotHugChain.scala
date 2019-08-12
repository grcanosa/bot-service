package com.grcanosa.bots.grupobot

import akka.actor.ActorRef
import com.bot4s.telegram.models.{InlineKeyboardButton, InlineKeyboardMarkup, ReplyKeyboardRemove}
import com.grcanosa.bots.grupobot.GrupoBotHugChain.HugChain
import com.grcanosa.telegrambot.bot.user.UserHandler
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.model.BotUser.PERMISSION_ALLOWED


object GrupoBotHugChain {

  case class HugChain(id: Long, users : List[UserHandler])

}


trait GrupoBotHugChain {

  import GrupoBotData._


  private val rwLock = List()

  var createdHugChains = 0L

  val hugChains = collection.mutable.Map.empty[Long,HugChain]

  def newHugChain(userHandler: UserHandler) = {
    rwLock.synchronized{
      createdHugChains += 1
      val hugChain = HugChain(createdHugChains,List(userHandler))
      hugChains.update(createdHugChains,hugChain)
      hugChain
    }
  }

  def getChain(chainId: Long) = {
    hugChains.get(chainId)
  }

  def increaseChain(uH: UserHandler, ch: HugChain) = {
    ch.copy(users = uH :: ch.users )
  }

  def permittedUserHandlers: Seq[UserHandler]

//  var gonzalosNumber = 4
//
//  val gonzalosHandlers = (1 to 5).map(n => UserHandler(BotUser(15111383,PERMISSION_ALLOWED,"Gonzalo"+n.toString,None,None),ActorRef.noSender))

  def getRemainingUserHandlers(hugChain: HugChain) = {
   // gonzalosNumber -= 1
    permittedUserHandlers.filter(! hugChain.users.contains(_)) //++ gonzalosHandlers.slice(0,gonzalosNumber)
  }

  def userHandlersInlineKeyboard(hugChain: HugChain) = {
    val remainingUsers = getRemainingUserHandlers(hugChain)
      InlineKeyboardMarkup( remainingUsers
      .map(u =>InlineKeyboardButton(u.user.name,Some(hugChainCallbackData(u.user.id,hugChain.id))))
        .sliding(2,2).toSeq
      )
  }

  def getHugChainMessage(hugChain: HugChain): (String, Option[InlineKeyboardMarkup]) = {
    hugChain.users match {
      case Nil => ("",None)
      case first :: Nil => chainStartedMessage(hugChain)
      case li if li.size == permittedUserHandlers.size  => chainEndedMessage(hugChain)
      case _ => chainContinueMessage(hugChain)
    }
  }

  def chainStartedMessage(chain: GrupoBotHugChain.HugChain) = {
    val txt = hugChainStartedText(chain.users.head.user.name) //Only one user, thats why I use head
    val keyboard = Some(userHandlersInlineKeyboard(chain))
    (txt, keyboard)
  }

  def chainEndedMessage(chain: GrupoBotHugChain.HugChain) = {
    val txt = hugChainEndText(chain) //Head is always the last one added
    (txt, None)
  }

  def chainContinueMessage(chain: GrupoBotHugChain.HugChain) = {
    val keyboard = Some(userHandlersInlineKeyboard(chain))
    val txt = hugChainContinueText(chain)
    (txt, keyboard)
  }


}
