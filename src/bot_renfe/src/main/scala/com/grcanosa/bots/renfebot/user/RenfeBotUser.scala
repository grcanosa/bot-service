package com.grcanosa.bots.renfebot.user

import java.time.format.DateTimeFormatter

import com.bot4s.telegram.methods.{EditMessageReplyMarkup, SendMessage}
import com.bot4s.telegram.models.Message
import com.grcanosa.bots.renfebot.bot.RenfeBot.{AddTripToDao, CheckTripForUsers}
import com.grcanosa.bots.renfebot.model.Journey
import com.grcanosa.bots.renfebot.user.RenfeBotUser.RenfeBotUserState
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.utils.LazyBotLogging


object RenfeBotUser {

  sealed abstract trait UserState

  case object START_STATE extends UserState
  case object SELECT_ORIGIN_STATE extends UserState
  case object SELECT_DEST_STATE extends UserState
  case object SELECT_DATE_STATE extends UserState


  sealed abstract trait UserAction
  case object NO_ACTION extends UserAction
  case object CONSULTA_AHORA extends UserAction
  case object CONSULTA_PERIODICA extends UserAction

  case class RenfeBotUserState(state: UserState
                               , action: UserAction
                               , origin: Option[String]
                               , dest: Option[String]
                               , date: Option[String])


  val START_USER_STATE = RenfeBotUserState(START_STATE,NO_ACTION,None,None,None)

  case class RenfeBotEventResponse(renfeBotUser: RenfeBotUser, responses: Seq[Any])

  def apply(botUser: BotUser) = {
    new RenfeBotUser(botUser,START_USER_STATE)
  }


}

class RenfeBotUser(val botUser: BotUser
                   , val state: RenfeBotUserState
                   )
  extends LazyBotLogging
{

  import RenfeBotUser._
  import com.grcanosa.bots.renfebot.bot.RenfeBotData._

  def menuMessage(): RenfeBotEventResponse = {
    RenfeBotEventResponse(
    new RenfeBotUser(botUser, START_USER_STATE)
      ,Seq(SendMessage(botUser.id,menuText,replyMarkup = Some(menuKeyboard)))
    )
  }

  def cancelMessage(): RenfeBotEventResponse = {
    RenfeBotEventResponse(
    new RenfeBotUser(botUser, START_USER_STATE)
      ,Seq(SendMessage(botUser.id,cancelText,replyMarkup = Some(removeKeyboard)))
    )
  }

  def processMessage(msg: Message): RenfeBotEventResponse = {
    state.state match {
      case START_STATE => processMessageStartState(msg)
      case SELECT_ORIGIN_STATE => processOriginStation(msg)
      case SELECT_DEST_STATE => processDestStation(msg)

    }
  }

  private def processOriginStation(msg: Message): RenfeBotEventResponse = {
    botlog.info(s"Select ${msg.text} as origin station")
    msg.text match {
      case None => RenfeBotEventResponse(
        new RenfeBotUser(botUser,state)
        ,Seq(SendMessage(botUser.id,respuestaNoValidaText))
      )
      case Some(txt) => RenfeBotEventResponse(
        new RenfeBotUser(botUser
        , state.copy(state = SELECT_DEST_STATE,origin = Some(txt)))
        , Seq(
            SendMessage(botUser.id,selectDestText,replyMarkup = Some(stationsKeyboard))
          )
        )
    }
  }

  private def processDestStation(msg: Message): RenfeBotEventResponse = {
    botlog.info(s"Select ${msg.text} as dest station")
    msg.text match {
      case None => RenfeBotEventResponse(
        new RenfeBotUser(botUser,state)
        ,Seq(SendMessage(botUser.id,respuestaNoValidaText))
      )
      case Some(txt) =>
        RenfeBotEventResponse(
        new RenfeBotUser(botUser
          , state.copy(state = SELECT_DATE_STATE,dest = Some(txt))
        )
          , Seq(
            SendMessage(botUser.id,selectedTripOriginDepartureText(state.origin.getOrElse(""),txt),replyMarkup = Some(removeKeyboard))
            , SendMessage(botUser.id,selectDateText,replyMarkup = Some(createCalendar()))
          )
        )
    }
  }

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def processKeyboardCallbackData(messageId: Int,data: String): RenfeBotEventResponse = {
    botlog.info(s"Processing callback data $data")
    processCallbackData(data) match {
      case (None, None) => RenfeBotEventResponse(new RenfeBotUser(botUser,state),Seq.empty)
      case (Some(newKeyboard), None) => RenfeBotEventResponse(
        new RenfeBotUser(botUser,state)
        ,Seq(
        EditMessageReplyMarkup(Some(botUser.id),Some(messageId),replyMarkup = Some(newKeyboard))
        )
      )
      case (None, Some(date)) => processDate(date.format(dateFormatter))
      case _ => RenfeBotEventResponse(new RenfeBotUser(botUser,state),Seq.empty)
    }
  }

  private def processSelectedJourney(origin: String, dest: String, date: String): RenfeBotEventResponse = {
    val journey = Journey(origin,dest,date,None)
    state.action match {
      case CONSULTA_AHORA =>{ RenfeBotEventResponse(new RenfeBotUser(botUser, START_USER_STATE),
        Seq(SendMessage(botUser.id,selectedTripFullText(state.origin.get,state.dest.get,date),replyMarkup = Some(removeKeyboard))
          , SendMessage(botUser.id,performinCheckNowText,replyMarkup = Some(removeKeyboard))
          , CheckTripForUsers(journey,Seq(botUser.id))
      ) )
      }
      case CONSULTA_PERIODICA => { RenfeBotEventResponse(new RenfeBotUser(botUser, START_USER_STATE),
        Seq(SendMessage(botUser.id,selectedTripFullText(state.origin.get,state.dest.get,date),replyMarkup = Some(removeKeyboard))
          , SendMessage(botUser.id,addingTripToDaoText,replyMarkup = Some(removeKeyboard))
          , AddTripToDao(botUser, journey)
          )
      )
      }
    }
  }

  private def processDate(date: String): RenfeBotEventResponse = {
    (state.origin,state.dest) match {
      case (Some(ori),Some(des)) => processSelectedJourney(ori,des,date)
      case _ =>  RenfeBotEventResponse(this,Seq.empty)
    }
  }


  private def processMessageStartState(msg: Message): RenfeBotEventResponse = {
    msg.text match {
      case Some(ConsultaAhoraMenuText) => { RenfeBotEventResponse(
        new RenfeBotUser(botUser, state.copy(state = SELECT_ORIGIN_STATE, action = CONSULTA_AHORA) )
          , Seq(
            SendMessage(botUser.id,hacerConsultaAhoraText,replyMarkup = Some(removeKeyboard))
            , SendMessage(botUser.id,selectOriginText,replyMarkup = Some(stationsKeyboard))
          )
        )
      }
      case Some(ConsultaPeriodicaMenuText) => { RenfeBotEventResponse(
        new RenfeBotUser(botUser, state.copy(state = SELECT_ORIGIN_STATE, action = CONSULTA_PERIODICA) )
        , Seq(
          SendMessage(botUser.id,addConsultaPeriodicaText,replyMarkup = Some(removeKeyboard))
          , SendMessage(botUser.id,selectOriginText,replyMarkup = Some(stationsKeyboard))
        )
      )
      }
      case Some(EliminarConsultaPeriodicaMenuText) => {
        ???
      }
      case Some(VerConsultaPeriodicaMenuText) => { //RenfeBotEventResponse(
        ???
      }
      case Some(_) => {
        botlog.info(s"Received unexpected message")
        RenfeBotEventResponse(
          this,
          Seq(SendMessage(botUser.id,respuestaNoValidaText,replyMarkup = Some(removeKeyboard)))
        )
      }
      case None => {
        RenfeBotEventResponse(
        new RenfeBotUser(botUser,state)
          ,Seq(SendMessage(botUser.id,respuestaNoValidaText))
        )
      }
    }
  }

}
