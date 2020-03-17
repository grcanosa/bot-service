package com.grcanosa.bots.renfebot.user

import java.time.format.DateTimeFormatter

import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.{Message, ReplyMarkup}
import com.grcanosa.bots.renfebot.user.RenfeBotUser.{RenfeBotUserState, START_STATE, UserState}
import com.grcanosa.telegrambot.model.BotUser

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


  def apply(botUser: BotUser) = {
    new RenfeBotUser(botUser,START_USER_STATE,Seq.empty)
  }


}

class RenfeBotUser(val botUser: BotUser
                   , val state: RenfeBotUserState
                   , val responses: Seq[Any]) {

  import RenfeBotUser._
  import com.grcanosa.bots.renfebot.RenfeBotData._

  def menuMessage(): RenfeBotUser = {
    new RenfeBotUser(botUser, START_USER_STATE,Seq(SendMessage(botUser.id,menuText,replyMarkup = Some(menuKeyboard))))
  }

  def cancelMessage(): RenfeBotUser = {
    new RenfeBotUser(botUser, START_USER_STATE,Seq(SendMessage(botUser.id,cancelText,replyMarkup = Some(removeKeyboard))))
  }

  def processMessage(msg: Message): RenfeBotUser = {
    state.state match {
      case START_STATE => processMessageStartState(msg)
      case SELECT_ORIGIN_STATE => processOriginStation(msg)
      case SELECT_DEST_STATE => processDestStation(msg)

    }
  }

  private def processOriginStation(msg: Message) = {
    msg.text match {
      case None => new RenfeBotUser(botUser,state,Seq(SendMessage(botUser.id,respuestaNoValidaText)))
      case Some(txt) =>
        new RenfeBotUser(botUser
        , state.copy(state = SELECT_DEST_STATE,origin = Some(txt))
        , Seq(
            SendMessage(botUser.id,selectDestText,stationsKeyboard)
          )
        )
    }
  }

  private def processDestStation(msg: Message) = {
    msg.text match {
      case None => new RenfeBotUser(botUser,state,Seq(SendMessage(botUser.id,respuestaNoValidaText)))
      case Some(txt) =>
        new RenfeBotUser(botUser
          , state.copy(state = SELECT_DATE_STATE,dest = Some(txt))
          , Seq(
            SendMessage(botUser.id,selectedTrip(state.origin.getOrElse(""),state.dest.getOrElse("")))
            , SendMessage(botUser.id,selectDateText,createCalendar())
          )
        )
    }
  }

  val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  private def processKeyboardCallbackData(data: String) = {
    processCallbackData(data) match {
      case (None, None) => new RenfeBotUser(botUser,state,Seq.empty)
      case (Some(newKeyboard), None) =>
      case (None, Some(date)) => processDate(date.format(dateFormatter))
      case _ => new RenfeBotUser(botUser,state,Seq.empty)
    }
  }

  private def processDate(date: String) = {
    new RenfeBotUser(botUser,
      START_USER_STATE,
      Seq(
        SendMessage(botUser.id,,removeKeyboard)
      )
  }


  private def processMessageStartState(msg: Message): RenfeBotUser = {
    msg.text match {
      case Some(ConsultaAhoraMenuText) => {
        new RenfeBotUser(botUser
          , state.copy(state = SELECT_ORIGIN_STATE, action = CONSULTA_AHORA)
          , Seq(
            SendMessage(botUser.id,hacerConsultaAhoraText,removeKeyboard)
            , SendMessage(botUser.id,selectOriginText,stationsKeyboard)
          ))
      }
      case Some(ConsultaPeriodicaMenuText) => {
        ???
      }
      case Some(EliminarConsultaPeriodicaMenuText) => {
        ???
      }
      case Some(VerConsultaPeriodicaMenuText) => {
        ???
      }
      case None => {
        new RenfeBotUser(botUser,state,Seq(SendMessage(botUser.id,respuestaNoValidaText)))
      }
    }
  }

}
