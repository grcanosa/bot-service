package com.grcanosa.telegrambot.dao

import com.grcanosa.telegrambot.model.{BotUser, Interaction}

import scala.concurrent.Future

trait InteractionDao {


  def insert(interaction: Interaction): Future[Boolean]


  def getAll(): Future[Seq[Interaction]]
}