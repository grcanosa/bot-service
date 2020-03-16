package com.grcanosa.bots.grupobot.utils

import com.typesafe.config.{Config, ConfigFactory}

object GrupoUtils {

  lazy val configGrupoOlmo: Config = {
    ConfigFactory.load("grupo_bot.conf")
  }

  lazy val configBaileGrupo = {
    ConfigFactory.load("grupo_baile_bot.conf")
  }

}
