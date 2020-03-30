package com.grcanosa.bots.webchecker.bot

import com.grcanosa.telegrambot.bot.BotWithAdmin

trait WebCheckerDefaultResponses {
  this: BotWithAdmin =>

  override def startCmdResponse(name: String): String = {
    s"Hola $name, me encargo de enviarte avisos sobre páginas web."
  }

  override def helpCmdResponse(name: String): String = {
    s"Por ahora no puedo hacer nada más que avisarte "
  }

//  override def userNotAllowedResponse(name: String): String = {
//
//  }
//
//  override def userRequestPermissionResponse(name: String): String = {
//
//  }
}
