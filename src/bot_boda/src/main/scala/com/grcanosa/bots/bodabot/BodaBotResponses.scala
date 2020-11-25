package com.grcanosa.bots.bodabot

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime, Period}

import com.grcanosa.telegrambot.bot.BotWithAdmin

trait BodaBotResponses { this: BotWithAdmin =>

  val weddingDate: LocalDateTime = LocalDateTime.of(2021,7,24,12,0,0)

  def daysToWedding() = {
    ChronoUnit.DAYS.between(LocalDateTime.now(),weddingDate)
  }

  def minutesToWedding() = {
    ChronoUnit.MINUTES.between(LocalDateTime.now(),weddingDate)
  }

  def secondsToWedding() = {
    ChronoUnit.SECONDS.between(LocalDateTime.now(),weddingDate)
  }

  def getGreetingBasedOnTime(name: String) = {
    LocalDateTime.now().getHour match {
      case n if n >= 6 && n <=14 => s"¡Buenos días $name!"
      case n if n >=15 && n <= 20 => s"¡Buenas tardes $name!"
      case n if n>= 21 && n<=2 => s"¡Buenas noches $name!"
      case _ => s"¿Qué haces despiert@ $name? ¿No ves la hora qué es?"
    }
  }

  import com.grcanosa.telegrambot.utils.BotUtils._
  override def helpCmdResponse(name: String) = {
    s"""Hola $name. Ésto es lo que puedo hacer:
      |/start - Imprime el mensaje de bienvenida.
      |/help - Imprime esta ayuda.
      |/cuando - :date:
      |/donde - :pushpin:
    """.stripMargin.emojize
  }

  override def startCmdResponse(name: String): String = {
    s"¡Bienvenido al bot de la Boda de Mer e Isa :two_women_holding_hands:! Escribe /help para ver qué puedo hacer.".emojize
  }


  val cuandoResponse = (_:String) => "La boda será el día 24 de Julio de 2021 a las 12h. :wedding: :couplekiss_woman_woman:".emojize

  val dondeResponse = (_:String) => "La boda será en Gijón. :sunrise_over_mountains:".emojize

  val holaResponse = (name: String) => {
    Seq(
      s"Hola $name, ¿qué tal todo? ¿Con ganas de boda? :partying_face:".emojize
      ,s"${getGreetingBasedOnTime(name)}".emojize
    ).chooseRandom()
  }

  val cuantoResponse = (name: String) => Seq(
    s"Tranquilo $name, ¡ya sólo quedan ${daysToWedding()} días para la boda! :partying_face:".emojize
    , s"Tranquilo $name, ¡ya sólo quedan ${minutesToWedding()} minutos para la boda! :champagne:".emojize
    ,s"Tranquilo $name, ¡ya sólo quedan ${secondsToWedding()} segundos para la boda! :fireworks:".emojize
  ).chooseRandom()


  private val saeiResponseSeq = Seq(
    s"Saeeeeeeeeeeeeiiiiiiiiiiiiiiiiiiii :lips:. ¡Viva Marian!".emojize
    , s":lips::lips::lips::lips::lips::lips::lips::lips::lips::lips::lips::lips::lips::lips::lips::lips::lips::lips::lips:".emojize
  )

  val saeiResponse = (name: String) => saeiResponseSeq.chooseRandom()


  def unknownResponse(name: String) = Seq(
    s"Lo siento $name, no sé qué me quieres decir. :sweat_smile:".emojize
  ).chooseRandom()

  val quienResponse = (name: String) => Seq(
    s"..."
  ).chooseRandom()

  private val queVivanLasNoviasResponseSeq = Seq(
    s"¡Que vivan las novias! :partying_face::champagne::partying_face::champagne::partying_face::champagne::partying_face::champagne:".emojize
  )

  val queVivanLasNoviasResponse = (name: String) => queVivanLasNoviasResponseSeq.chooseRandom()
}
