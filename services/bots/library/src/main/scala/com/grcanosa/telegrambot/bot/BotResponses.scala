package com.grcanosa.telegrambot.bot

trait BotResponses {


  def userNotAllowedResponse(name: String) ={
    s"$name, you do not have access to this bot."
  }

  def userRequestPermissionResponse(name: String) = {
    s"$name, asking admin for permission, please wait to be notified."
  }

  def startCmdResponse(name: String) = {
    s"Default start message $name"
  }

  def helpCmdResponse(name: String) = {
    s"Default help message $name"
  }

  def permissionGrantedResponse = {
    s"The admin has given you permission. Write /start to begin"
  }


}
