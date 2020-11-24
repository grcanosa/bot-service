package com.grcanosa.bots.app

import com.bot4s.telegram.api.{AkkaDefaults, AkkaImplicits}
import com.grcanosa.bots.bodabot.BodaBot
import com.grcanosa.bots.grcanosabot.GrcanosaBot
import com.grcanosa.telegrambot.utils.LazyBotLogging
import com.typesafe.config.ConfigFactory

object BodaBotApp extends App with LazyBotLogging with AkkaDefaults{

  botlog.info("Starting Bot")
  implicit val ec = system.getDispatcher
  val config = ConfigFactory.load("bodabot.conf")
  val b = BodaBot(config)

  b.run()

}
