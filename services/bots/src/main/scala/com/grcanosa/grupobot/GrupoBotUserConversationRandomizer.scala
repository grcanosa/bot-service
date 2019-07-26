package com.grcanosa.grupobot

import akka.actor.Cancellable
import com.grcanosa.telegrambot.bot.user.{UserHandler, UserRegistry}

trait GrupoBotUserConversationRandomizer extends UserRegistry {

  import com.grcanosa.telegrambot.utils.BotUtils._

  implicit class ConversationComparer(conv: Conversation){
    def isNotEqual(conv2: Conversation) = {
      conv.uh1.user.id != conv2.uh1.user.id || conv.uh2.user.id != conv2.uh2.user.id
    }
  }


  case class Conversation(uh1: UserHandler
                          , uh2: UserHandler
                          , cancel: Option[Cancellable])

  case class CancelConversation(conversation: Conversation)

  var userConversations = Seq.empty[Conversation]

  def getUserConversation(userH: UserHandler) = {
    getConversationForUser(userH) match {
      case None => assignNewConversation(userH)
      case some => some
    }
  }


  def getConversationForUser(userH: UserHandler) = {
    userConversations.find{
      conv => conv.uh1.user.id == userH.user.id || conv.uh2.user.id == userH.user.id
    }
  }

  def assignNewConversation(userH: UserHandler) = {
    BOTLOG.info(s"Assigning new conversation to user: ${userH.user.name}")
    val destUH = getUsersWithNoConversations()
      .filter(uh => uh.user.id != userH.user.id)
      .chooseRandom()

    destUH.map{ duh =>
      BOTLOG.info(s"Conversation assigned to ${duh.user.name}")
      val conv = Conversation(userH,duh,None)
      userConversations = conv +: userConversations
      conv
    }
  }

  def getUsersWithNoConversations() = {
    userHandlers.values.toSeq
      .filter(uh => getConversationForUser(uh).isEmpty)
  }


  def getConversationDestination(from: UserHandler, conversation: Conversation) = {
    if(conversation.uh1.user.id == from.user.id){
      conversation.uh2
    }else{
      conversation.uh1
    }
  }

}
