package com.grcanosa.bots.renfebot

import com.bot4s.telegram.models.{InlineKeyboardMarkup, KeyboardButton, ReplyKeyboardMarkup, ReplyKeyboardRemove}
import com.grcanosa.telegrambot.utils.CalendarKeyboard

object RenfeBotData extends CalendarKeyboard{

  import com.grcanosa.telegrambot.utils.BotUtils._

  lazy val menuText = "Hola, ¿qué quieres hacer hoy?"

  lazy val cancelText = "Operación cancelada."

  lazy val ConsultaAhoraMenuText = "Hacer consulta ahora.".emojize
  lazy val ConsultaPeriodicaMenuText = "Añadir consulta periódica.".emojize
  lazy val EliminarConsultaPeriodicaMenuText = "Eliminar consulta periódica.".emojize
  lazy val VerConsultaPeriodicaMenuText = "Ver consultas periódicas.".emojize

  lazy val menuKeyboard: ReplyKeyboardMarkup = ReplyKeyboardMarkup(Seq(
      Seq(KeyboardButton(ConsultaAhoraMenuText))
    , Seq(KeyboardButton(ConsultaPeriodicaMenuText))
    , Seq(KeyboardButton(EliminarConsultaPeriodicaMenuText))
    , Seq(KeyboardButton(VerConsultaPeriodicaMenuText))
  ))

  lazy val removeKeyboard: ReplyKeyboardRemove = ReplyKeyboardRemove(true)

  lazy val respuestaNoValidaText = ""

  lazy val stations = Seq(
     "MADRID-PUERTA DE ATOCHA"
   , "SEVILLA-SANTA JUSTA"
   , "BARCELONA-SANTS"
   , "LLEIDA"
   , "MALAGA MARIA ZAMBRANO"
   , "VALENCIA JOAQUIN SOROLLA"
  )

  lazy val stationsKeyboard: ReplyKeyboardMarkup = ReplyKeyboardMarkup(
    stations
    .map(KeyboardButton(_))
    .grouped(2)
    .toSeq
  )

  lazy val hacerConsultaAhoraText = "Ok, voy a hacer una consulta puntual.".emojize

  lazy val selectOriginText = "Introduce estación origen :train2:".emojize
  lazy val selectDestText = "Introduce estación destino :train2:".emojize
  lazy val selectDateText = "Elige ahora la fecha del viaje :calendar:".emojize

  lazy val selectedTripOriginDeparture = (ori: String, dest: String) => s"Has seleccionado el trayecto $ori -> $dest.".emojize

  lazy val selectedTripFull = (ori: String, dest: String, date: String) => s"Has seleccionado el trayecto $ori -> $dest para el dia $date".emojize


  def dateKeyboard(): InlineKeyboardMarkup = {

  }
}
