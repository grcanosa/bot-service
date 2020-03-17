package com.grcanosa.telegrambot.bot

import com.bot4s.telegram.models.{KeyboardButton, ReplyKeyboardMarkup, ReplyKeyboardRemove}
import com.grcanosa.telegrambot.model.BotUser

trait BotKeyboards {


  val removeKeyboard = ReplyKeyboardRemove(true)

  def permissionKeyboard(user: BotUser) = {
    ReplyKeyboardMarkup(Seq(
      Seq(KeyboardButton(s"/permission ${user.id} ALLOW")),
      Seq(KeyboardButton(s"/permission ${user.id} NOTALLOW"))
      )
    )
  }



}
