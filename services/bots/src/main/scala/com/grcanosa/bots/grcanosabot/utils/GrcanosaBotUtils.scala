package com.grcanosa.bots.grcanosabot.utils

import com.typesafe.config.{Config, ConfigFactory}

object GrcanosaBotUtils {

  lazy val configGrcanosa: Config = {
    ConfigFactory.load("grcanosabot.conf")
  }

}
