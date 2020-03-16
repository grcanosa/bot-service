package com.grcanosa.telegrambot.dao.mongo


import com.grcanosa.telegrambot.utils.LazyBotLogging
import com.mongodb.client.result.UpdateResult
import org.mongodb.scala.{Completed, SingleObservable}


import scala.concurrent.ExecutionContext

trait MongoResultsMappings extends LazyBotLogging{

  def executionContext: ExecutionContext

  implicit val _ec = executionContext



  implicit class SingleObservableCompleted(value: SingleObservable[Completed]){

    def toBooleanFuture() = {
      value.headOption().map{
        case Some(Completed()) => true
        case None => false
      }.recover{
        case e => botlog.error(s"Insertion exception: ${e.toString}"); false
      }
    }
  }

  implicit class SingleObservableUpdateResult(value: SingleObservable[UpdateResult]){

    def toBooleanFuture() = {
      value.headOption().map{
        case None => botlog.debug("Update incorrect");false
        case Some(ur) if ur.wasAcknowledged() => botlog.debug("Update correct");true
        case _ => botlog.debug("Update incorrect"); false
      }.recover{
        case e => botlog.info(s"Update exception: ${e.toString}"); false
      }
    }

  }

}
