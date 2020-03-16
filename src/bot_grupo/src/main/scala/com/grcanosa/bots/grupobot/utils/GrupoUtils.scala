package com.grcanosa.bots.grupobot.utils

import com.typesafe.config.{Config, ConfigFactory}

object GrupoUtils {

  lazy val configGrupo: Config = {
    ConfigFactory.load("grupo_bot.conf")
  }

}
