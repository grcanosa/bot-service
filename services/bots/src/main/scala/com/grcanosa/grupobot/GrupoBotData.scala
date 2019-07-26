package com.grcanosa.grupobot

import com.vdurmont.emoji.EmojiParser

import scala.concurrent.duration._

object GrupoBotData {

  import com.grcanosa.telegrambot.utils.BotUtils._


  implicit class BotStrings(s: String){
    def bottext: String = EmojiParser.parseToUnicode(":robot_face::speech_balloon: "+s)
  }


  val conversationDuration = 15 seconds


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
