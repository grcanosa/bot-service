package com.grcanosa.telegrambot.dao

import com.grcanosa.telegrambot.model.BotUser

import scala.concurrent.Future

trait BotUserDao {


  def insertUser(user: BotUser): Future[Boolean]

  def updateUser(user: BotUser): Future[Boolean]

  def removeUser(user: BotUser): Future[Boolean]

  def getUsers(): Future[Seq[BotUser]]

  def getUser(userId: Long) : Future[Option[BotUser]]

}