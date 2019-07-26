package com.grcanosa.telegrambot.dao

trait BotDao  {

  implicit def botUserDao: BotUserDao

  implicit def interactionDao: InteractionDao
}
