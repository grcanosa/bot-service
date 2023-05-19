package com.grcanosa.bots.app

import com.bot4s.telegram.api.AkkaDefaults
import com.grcanosa.telegrambot.utils.LazyBotLogging
import com.typesafe.config.ConfigFactory
import com.grcanosa.bots.rociobot
import com.grcanosa.bots.rociobot.Rocio2Bot


object RocioBotApp extends App with LazyBotLogging with AkkaDefaults{

  botlog.info("Starting Bot")
  implicit val ec = system.getDispatcher
  val config = ConfigFactory.load("rociobot.conf")
  val b = Rocio2Bot(config)

  b.run()

}
