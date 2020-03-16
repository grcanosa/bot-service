package com.grcanosa.bots.grcanosabot

import scala.io.Source

trait GrcanosaFrases {

  import com.grcanosa.bots.grcanosabot.utils.GrcanosaBotUtils._
  import com.grcanosa.telegrambot.utils.BotUtils._

  val piropos: Seq[String] = configGrcanosa.getString("bot.frases.filePiropos").getResourceFileLinesAsSeq()

  val piroposRealmente = configGrcanosa.getString("bot.frases.filePiroposRealmente").getResourceFileLinesAsSeq()

  val startDayMessages = configGrcanosa.getString("bot.frases.fileStartDayMessages").getResourceFileLinesAsSeq()

  //piropos.foreach(p => println(s"PIROPO: $p"))

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
