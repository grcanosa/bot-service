package com.grcanosa.bots.app

import com.grcanosa.bots.grcanosabot.GrcanosaBot
import com.grcanosa.telegrambot.utils.LazyBotLogging

object GrcanosaBotApp extends App with LazyBotLogging{

  botlog.info("Starting Bot")

  GrcanosaBot.bot.run()

}
