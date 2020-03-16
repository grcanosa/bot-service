package com.grcanosa.telegrambot.dao.mongo


import com.mongodb.client.result.UpdateResult
import org.mongodb.scala.{Completed, SingleObservable}

import scala.concurrent.ExecutionContext

trait MongoResultsMappings {

  def executionContext: ExecutionContext

  implicit val _ec = executionContext

  import com.grcanosa.telegrambot.utils.BotUtils.BOTLOG

  implicit class SingleObservableCompleted(value: SingleObservable[Completed]){

    def toBooleanFuture() = {
      value.headOption().map{
        case Some(Completed()) => true
        case None => false
      }.recover{
        case e => BOTLOG.error(s"Insertion exception: ${e.toString}"); false
      }
    }
  }

  implicit class SingleObservableUpdateResult(value: SingleObservable[UpdateResult]){

    def toBooleanFuture() = {
      value.headOption().map{
        case None => BOTLOG.debug("Update incorrect");false
        case Some(ur) if ur.wasAcknowledged() => BOTLOG.debug("Update correct");true
        case _ => BOTLOG.debug("Update incorrect"); false
      }.recover{
        case e => BOTLOG.info(s"Update exception: ${e.toString}"); false
      }
    }

  }

}
