package com.grcanosa.telegrambot.model

import com.bot4s.telegram.models.User
import com.grcanosa.telegrambot.model.BotUser.BotUserPermission

case class BotUser(id: Long
                   ,permission: BotUserPermission
                  , name: String)


object BotUser{


  sealed abstract class BotUserPermission(val value: String)
  case object PERMISSION_NOT_SET extends BotUserPermission("NOT_SET")
  case object PERMISSION_ALLOWED extends BotUserPermission("ALLOWED")
  case object PERMISSION_NOT_ALLOWED extends BotUserPermission("NOT_ALLOWED")

  def getPermission(value: String) = {
    value match {
      case PERMISSION_ALLOWED.value => PERMISSION_ALLOWED
      case PERMISSION_NOT_ALLOWED.value => PERMISSION_NOT_ALLOWED
      case PERMISSION_NOT_SET.value => PERMISSION_NOT_SET
      case _ => PERMISSION_NOT_SET
    }
  }

  def fromUser(user:User) = {
      BotUser(user.id,PERMISSION_NOT_SET,user.firstName)
  }
}