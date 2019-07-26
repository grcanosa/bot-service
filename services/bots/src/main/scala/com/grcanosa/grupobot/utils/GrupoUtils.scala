package com.grcanosa.grupobot.utils

import com.typesafe.config.{Config, ConfigFactory}

object GrupoUtils {

  lazy val configGrupo: Config = {
    ConfigFactory.load("grupobot.conf")
  }

}
