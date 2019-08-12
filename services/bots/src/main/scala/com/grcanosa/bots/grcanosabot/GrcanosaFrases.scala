package com.grcanosa.bots.grcanosabot

import scala.io.Source

trait GrcanosaFrases {

  import com.grcanosa.bots.grcanosabot.utils.GrcanosaBotUtils._

  import com.grcanosa.telegrambot.utils.BotUtils._

  lazy val piropos: Seq[String] = configGrcanosa.getString("bot.frases.filePiropos").getLinesAsSeq

  lazy val piroposRealmente = configGrcanosa.getString("bot.frases.filePiroposRealmente").getLinesAsSeq

  lazy val startDayMessages = configGrcanosa.getString("bot.frases.fileStartDayMessages").getLinesAsSeq

  def getAlgoBonito: String = {
    piropos.chooseRandom().getOrElse("")
  }

  def getAlgoRealmenteBonito = {
    piroposRealmente.chooseRandom().getOrElse("")
  }

  def getStartDayMessage = {
    startDayMessages.chooseRandom().getOrElse("")
  }
}
