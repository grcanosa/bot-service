package com.grcanosa.bots.bodabot

import com.grcanosa.telegrambot.bot.BotWithAdmin

trait BodaBotResponses { this: BotWithAdmin =>
  import com.grcanosa.telegrambot.utils.BotUtils._
  override def helpCmdResponse(name: String) = {
    s"""Hola $name. Ésto es lo que puedo hacer:
      |/start - Imprime el mensaje de bienvenida.
      |/help - Imprime esta ayuda.
      |/cuando - :date:
      |/donde - :pushpin:
    """.stripMargin.emojize
  }

  override def startCmdResponse(name: String): String = {
    s"¡Bienvenido al bot de la Boda de Mer e Isa! Escribe /help para ver qué puedo hacer."
  }

}
