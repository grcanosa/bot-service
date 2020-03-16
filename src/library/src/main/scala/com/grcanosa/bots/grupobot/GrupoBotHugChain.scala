package com.grcanosa.bots.grupobot

import akka.actor.ActorRef
import com.bot4s.telegram.models.{InlineKeyboardButton, InlineKeyboardMarkup, Message, ReplyKeyboardRemove}
import com.grcanosa.bots.grupobot.GrupoBotHugChain.HugChain
import com.grcanosa.telegrambot.bot.user.{UserHandler, UserRegistry}
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.model.BotUser.PERMISSION_ALLOWED

import scala.util.Random


object GrupoBotHugChain {
  import GrupoBotData._
  case class HugChain(id: String, users : List[UserHandler])

  val random = new Random(java.time.Instant.now().toEpochMilli)

  def getRandomHugChainID="HG_"+java.time.Instant.now().toEpochMilli.toString+"_"+random.nextInt(100)

  def increaseChain(uH: UserHandler, ch: HugChain) = {
    ch.copy(users = uH :: ch.users )
  }


  def getRemainingUserHandlers(hugChain: HugChain, permittedUsers: Seq[UserHandler]) = {
    // gonzalosNumber -= 1
    permittedUsers.filter(! hugChain.users.contains(_)) //++ gonzalosHandlers.slice(0,gonzalosNumber)
  }

  def userHandlersInlineKeyboard(hugChain: HugChain, permittedUsers: Seq[UserHandler]) = {
    val remainingUsers = getRemainingUserHandlers(hugChain, permittedUsers)
    InlineKeyboardMarkup( remainingUsers
      .map(u =>InlineKeyboardButton(u.user.name,Some(hugChainCallbackData(u.user.id,hugChain.id))))
      .sliding(2,2).toSeq
    )
  }

  def getHugChainMessage(hugChain: HugChain, permittedUsers: Seq[UserHandler]): (String, Option[InlineKeyboardMarkup]) = {
    hugChain.users match {
      case Nil => ("",None)
      case first :: Nil => chainStartedMessage(hugChain,permittedUsers)
      case li if li.size == permittedUsers.size  => chainEndedMessage(hugChain)
      case _ => chainContinueMessage(hugChain, permittedUsers)
    }
  }

  def chainStartedMessage(chain: GrupoBotHugChain.HugChain, permittedUsers: Seq[UserHandler]) = {
    val txt = hugChainStartedText(chain.users.head.user.name) //Only one user, thats why I use head
    val keyboard = Some(userHandlersInlineKeyboard(chain,permittedUsers))
    (txt, keyboard)
  }

  def chainEndedMessage(chain: GrupoBotHugChain.HugChain) = {
    val txt = hugChainEndText(chain) //Head is always the last one added
    (txt, None)
  }

  def chainContinueMessage(chain: GrupoBotHugChain.HugChain,permittedUsers: Seq[UserHandler]) = {
    val keyboard = Some(userHandlersInlineKeyboard(chain,permittedUsers))
    val txt = hugChainContinueText(chain)
    (txt, keyboard)
  }

}


trait GrupoBotHugChain {this : UserRegistry =>

  import GrupoBotData._
  import GrupoBotHugChain._
  private val rwLock = List()

  val hugChains = collection.mutable.Map.empty[String,HugChain]



  def newHugChain(userHandler: UserHandler) = {
    rwLock.synchronized{
      val randId = getRandomHugChainID
      val hugChain = HugChain(randId,List(userHandler))
      hugChains.update(randId,hugChain)
      hugChain
    }
  }

  def getChain(chainId: String) = {
    hugChains.get(chainId)
  }

  def updateChain(ch: HugChain) = {
    hugChains.update(ch.id,ch)
  }


  def getCallbackData(data: Option[String]) = {
    for {
      cbkData <- parseCallbackInfo(data)
      user <- getUser(cbkData._2)
      chain <- getChain(cbkData._1)
    } yield (user,chain)
  }

  def processHugChainCallbackData(message: Option[Message],data: Option[String]) = {
    for {
      msg <- message
      (user,chain) <- getCallbackData(data)
      newChain = increaseChain(user,chain)
      txtKeyboard = getHugChainMessage(newChain,permittedUserHandlers)
    } yield(msg,newChain, user,txtKeyboard._1,txtKeyboard._2)
  }









}
