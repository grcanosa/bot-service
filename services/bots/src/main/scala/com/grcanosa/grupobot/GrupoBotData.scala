package com.grcanosa.grupobot

import com.grcanosa.grupobot.utils.GrupoUtils
import com.vdurmont.emoji.EmojiParser

import scala.concurrent.duration._
import scala.util.Try

object GrupoBotData {

  import com.grcanosa.telegrambot.utils.BotUtils._
  import GrupoUtils._

  implicit class BotStrings(s: String){
    def bottext: String = EmojiParser.parseToUnicode(":robot_face::speech_balloon: "+s)
  }


  val conversationDuration = Try{configGrupo.getInt("grupobot.conversation.minutes")}.getOrElse(15) minutes

  BOTLOG.info(s"Conversation set to last $conversationDuration")

  val noConversationReadyText = (name: String) => {
    s"Ahora mismo no te puedo asignar una conversación $name, inténtalo de nuevo más tarde!".bottext
  }

  val noConversationAssigned = (name: String) => {
    s"$name, no tienes ninguna conversación asignada ahora mismo.".bottext
  }

  val conversationEndedText = {
    "Conversación finalizada.".bottext
  }

  val startText = {
    "Hola, soy el bot".bottext
  }

  val helpText = {
    """Esto es lo que puedo hacer:
      |/start - Imprime el mensaje de bienvenida
      |/help - Imprime esta ayuda.
      |/cancelconversation - Cancela la conversación actual
    """.stripMargin.bottext
  }

  val requestingPermissionText = "Pidiendo acceso a admin.".bottext

  val notAllowedText = "No tienes permiso para usar este bot.".bottext

}
