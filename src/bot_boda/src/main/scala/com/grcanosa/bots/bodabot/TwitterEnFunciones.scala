package com.grcanosa.bots.bodabot

import akka.Done
import akka.actor.Actor.Receive
import akka.actor.ActorRef
import com.bot4s.telegram.methods.SendMessage
import com.danielasfregola.twitter4s.TwitterRestClient
import com.grcanosa.bots.bodabot.TwitterEnFunciones.EnFuncionesTweet
import com.grcanosa.bots.bodabot.TwitterPalabras.{CheckPalabrasMentions, PalabrasTweet, PublishPalabrasTweetCountry, getWordChain}
import com.grcanosa.telegrambot.bot.BotWithAdmin

import java.time.{Duration, LocalDateTime}
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

object TwitterEnFunciones{

  case object EnFuncionesTweet
}

trait TwitterEnFunciones  {this: BotWithAdmin =>

  val enFuncionesTwitterClient: TwitterRestClient
  val selfActor: ActorRef
  import com.grcanosa.telegrambot.utils.BotUtils._

//
//  onCommand("/palabra"){implicit msg =>
//    allowedUser(Some("msg")) { uH =>
//      isAdmin{_ =>
//        selfActor ! PalabrasTweet
//      }()
//    }
//  }
//
//  onCommand("/newyear"){ implicit msg =>
//    allowedUser(Some("msg")) { uH =>
//      isAdmin{_ =>
//        val m = java.time.LocalDateTime.now().getMinute
//        botlog.info(s"Manually checking with minute $m")
//        publishCountries(Some(m))
//      }()
//    }
//  }




  val futEnFunciones: Future[Done] = akka.stream.scaladsl.Source.tick(5 seconds, 1 minute,"msg").runForeach { _ => {
    val now = LocalDateTime.now()
    if(now.getMinute == 0 && (now.getHour == 10)){
      selfActor ! EnFuncionesTweet
    }
  }
  }

  futEnFunciones.onComplete{
    case Success(_) => botlog.info(s"COMPLETED OK")
    case Failure(e) => botlog.error(s"COMPLETED ERROR",e)
  }






  def enFuncionesTwitterBehaviour: Receive = {
    case EnFuncionesTweet => {
      botlog.info(s"Creating daily tweet en funciones")
      createEnFuncionesDailyTweet().onComplete{
        case Success(tweet) => {
          botlog.info(s"Created $tweet")
          botActor ! SendMessage(adminId,"EnFunciones Tweet creado correctamente")
        }
        case Failure(e) => botlog.error(s"Error in tweet",e)
      }
    }
//    case PublishPalabrasTweetCountry(msg) => {
//      palabrasTwitterClient.createTweet(msg, latitude = None, longitude = None).onComplete{
//        case Success(t) => logger.info(s"Published $msg")
//        case Failure(e) => logger.error(s"Error publishing $msg",e)
//      }
//    }
//    case CheckPalabrasMentions => {
//      //botlog.info(s"Checking mentions")
//      checkPalabrasMentions()
//    }
  }

 // var lastCheckedTweet: Option[Long] = None





  val cgpjExpiredDate = LocalDateTime.of(2018,12,4,0,0,0)

  def createEnFuncionesDailyTweet() = {
    val now = LocalDateTime.now()
    val numberOfDaysExpired = Duration.between(cgpjExpiredDate,now).toDays
    val txt = s"El CGPJ lleva $numberOfDaysExpired en funciones."
    enFuncionesTwitterClient.createTweet(txt, latitude = None, longitude = None)
  }





//  //new year
//  def publishNewYear() = {
//    val countriesToPublish =
//
//    val countryCodes = Countries.zoneIds.flatMap{ z =>
//      val localDate = LocalDateTime.now(z)
//      if(localDate.getDayOfYear == 1
//        && localDate.getHour == 0
//        && localDate.getMinute == 0
//        && localDate.getSecond > 0 && localDate.getSecond < 30){
//        Some(z)
//      }else{
//        None
//      }
//    }
//  }
//
//  def publishCountries(min: Option[Int]) = {
//    val toPublish = Countries.getMessagesToPublish("Happy New Year to ",None,min)
//    toPublish.foreach(botlog.info(_))
//    if(toPublish.nonEmpty){
//      toPublish.foreach{ msg =>
//        selfActor ! PublishPalabrasTweetCountry(msg)
//      }
//    }
//
//    val toPublish2 = Countries.getMessagesToPublish("30 minutes to New Year in ",Some(30),min)
//    toPublish2.foreach(botlog.info(_))
//    if(toPublish2.nonEmpty){
//      toPublish2.foreach{ msg =>
//        selfActor ! PublishPalabrasTweetCountry(msg)
//      }
//    }
//  }


//  val futCountries: Future[Done] = akka.stream.scaladsl.Source.tick(5 seconds, 30 seconds,"msg").runForeach { _ => {
//    botlog.info(s"Checking to publish")
//    publishCountries(None)
//  }
//  }
//
//  futCountries.onComplete{
//    case Success(_) => botlog.info(s"COMPLETED OK")
//    case Failure(e) => botlog.error(s"COMPLETED ERROR",e)
//  }






}
