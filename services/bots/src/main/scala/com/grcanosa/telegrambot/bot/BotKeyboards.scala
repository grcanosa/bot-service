package com.grcanosa.telegrambot.bot

import com.bot4s.telegram.models.{KeyboardButton, ReplyKeyboardMarkup, ReplyKeyboardRemove}

trait BotKeyboards {


  val removeKeyboard = ReplyKeyboardRemove(true)

  def permissionKeyboard(userId: Long) = {
    ReplyKeyboardMarkup(Seq(
      Seq(KeyboardButton("/permission "+userId.toString+ " ALLOW")),
      Seq(KeyboardButton("/permission "+userId.toString+ " NOTALLOW"))
      )
    )
  }

}
