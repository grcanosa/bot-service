package com.grcanosa.telegrambot.utils

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory


trait LazyBotLogging {

  @transient
  protected lazy val botlog: Logger =
    Logger(LoggerFactory.getLogger(getClass.getName))


}
