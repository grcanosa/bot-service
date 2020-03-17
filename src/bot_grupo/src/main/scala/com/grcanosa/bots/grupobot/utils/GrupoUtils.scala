package com.grcanosa.bots.grupobot.utils

import com.typesafe.config.{Config, ConfigFactory}

object GrupoUtils {

  lazy val configGrupoOlmo: Config = {
    ConfigFactory.load("grupobot.conf")
  }


}
