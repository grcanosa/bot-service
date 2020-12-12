package com.grcanosa.bots.bodabot

import java.time.{LocalDateTime, Month}

import akka.Done
import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.stream.scaladsl.Source
import com.bot4s.telegram.methods.SendMessage
import com.danielasfregola.twitter4s.TwitterRestClient
import com.grcanosa.bots.bodabot.TwitterMessages.DailyTweet
import com.grcanosa.telegrambot.bot.BotWithAdmin
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object TwitterMessages{

  case object DailyTweet

}

trait TwitterMessages{ this: BotWithAdmin =>

  val twitterClient: TwitterRestClient
  val selfActor: ActorRef

  val f: Future[Done] = Source.tick(5 seconds, 1 minute,"msg").runForeach { _ => {
    val now = LocalDateTime.now()
    if(now.getHour == 9 && now.getMinute == 0){
      selfActor ! DailyTweet
    }
  }
  }

  f.onComplete{
    case Success(_) => botlog.info(s"COMPLETED OK")
    case Failure(e) => botlog.error(s"COMPLETED ERROR",e)
  }

  def bodaTwitterBehaviour: Receive = {
    case DailyTweet => {
      botlog.info(s"Creating daily tweet")
      createDailyTweet().onComplete{
        case Success(tweet) => {
          botlog.info(s"Created $tweet")
          botActor ! SendMessage(adminId,"Tweet creado correctamente")
        }
        case Failure(e) => botlog.error(s"Error in tweet",e)
      }
    }
  }

  def createDailyTweet() = {
    twitterClient.createTweet(getDailyTweetText(), latitude = None, longitude = None)
    //twitterClient.
  }


  def getDailyTweetText(): String = {
    val n = LocalDateTime.now()
    if(n.getYear == 2021 && n.getMonth == Month.JULY && n.getDayOfMonth == 24){
      s"¡¡¡¡¡Hoy es la boda de Mer e Isa!!!!!"
    }else{
      s"Hoy no es la boda de Mer e Isa."
    }
  }

}
