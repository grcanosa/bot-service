package com.grcanosa.grupobot

import java.time.LocalDateTime

import akka.actor.Cancellable
import com.grcanosa.grupobot.model.Conversation
import com.grcanosa.telegrambot.bot.user.{UserHandler, UserRegistry}
import com.grcanosa.telegrambot.model.BotUser.PERMISSION_ALLOWED

trait GrupoBotUserConversationRandomizer extends UserRegistry {

  import com.grcanosa.telegrambot.utils.BotUtils._

  implicit class ConversationComparer(conv: Conversation){
    def isNotEqual(conv2: Conversation) = {
      conv.uh1.user.id != conv2.uh1.user.id || conv.uh2.user.id != conv2.uh2.user.id
    }
  }




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
      val conv = Conversation(userH,duh,None,LocalDateTime.now().toString,LocalDateTime.now().toString)
      userConversations = conv +: userConversations
      conv
    }
  }

  def getUsersWithNoConversations() = {
    userHandlers.values.toSeq
        .filter(uh => uh.user.permission == PERMISSION_ALLOWED)
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
