package com.grcanosa.bots.bodabot

import akka.Done
import akka.actor.Actor.Receive
import akka.actor.{ActorRef, Timers}
import com.bot4s.telegram.methods.SendMessage
import com.danielasfregola.twitter4s.TwitterRestClient
import com.grcanosa.bots.bodabot.ExtraerPalabras.{getWordRegexList, path}
import com.grcanosa.bots.bodabot.TwitterMessages.DailyTweet
import com.grcanosa.bots.bodabot.TwitterPalabras.{CheckPalabrasMentions, PalabrasTweet, getWordChain}
import com.grcanosa.telegrambot.bot.BotWithAdmin
import com.grcanosa.telegrambot.utils.BotUtils

import java.time.LocalDateTime
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.util.{Failure, Success, Try}

object TwitterPalabras{


  lazy val palabras = Source.fromResource("palabras/palabras.txt").getLines.toList


  val allLeters = "[a-zA-ZáéíóíúÁÉÍÓÚ]"

  def substituteLetter(word: String, remIndex: Int, subs: String) = {
    val s = for{
      (c,i) <- word.zipWithIndex
    } yield {
      if(i == remIndex) subs
      else c.toString
    }
    s.mkString
  }

  def getWordRegexList(word: String) = {
    (0 until word.length).map { i =>
      substituteLetter(word, i, allLeters)
    }.reverse.map(_.r)
  }

  def getWordChain(word: String, allWords: List[String]): List[String] = {
    def getWordChainRecursive(lastWord: String, prevWords: List[String]): List[String] = {
      val newWord: Option[String] = getWordRegexList(lastWord)
        .foldLeft(Option.empty[String]) { case (opS, regex) =>
          if (opS.isDefined) opS
          else {
            allWords.find { w =>
              lastWord.length == w.length &&
                !prevWords.contains(w) &&
                regex.findFirstIn(w).isDefined
            }
          }
        }
      newWord match {
        case Some(newW) => getWordChainRecursive(newW,prevWords :+ newW)
        case None => prevWords
      }
    }
    getWordChainRecursive(word,List(word))
  }

  case object PalabrasTweet
  case object CheckPalabrasMentions

}

trait TwitterPalabras  {this: BotWithAdmin =>

  val palabrasTwitterClient: TwitterRestClient
  val selfActor: ActorRef
  import com.grcanosa.telegrambot.utils.BotUtils._

  def getNewChain() = {
    val w = TwitterPalabras.palabras.chooseRandom()
    TwitterPalabras.getWordChain(w,TwitterPalabras.palabras)
  }

  onCommand("/palabra"){implicit msg =>
    allowedUser(Some("msg")) { uH =>
      isAdmin{_ =>
        selfActor ! PalabrasTweet
      }()
    }
  }



  val futPalabras: Future[Done] = akka.stream.scaladsl.Source.tick(5 seconds, 1 minute,"msg").runForeach { _ => {
    val now = LocalDateTime.now()
    if(now.getMinute == 0 && (now.getHour == 9 || now.getHour == 14 || now.getHour == 19)){
      selfActor ! PalabrasTweet
    }
  }
  }

  futPalabras.onComplete{
    case Success(_) => botlog.info(s"COMPLETED OK")
    case Failure(e) => botlog.error(s"COMPLETED ERROR",e)
  }




  def palabrasTwitterBehaviour: Receive = {
    case PalabrasTweet => {
      botlog.info(s"Creating daily tweet")
      createPalabrasDailyTweet().onComplete{
        case Success(tweet) => {
          botlog.info(s"Created $tweet")
          botActor ! SendMessage(adminId,"Palabras Tweet creado correctamente")
        }
        case Failure(e) => botlog.error(s"Error in tweet",e)
      }
    }
    case CheckPalabrasMentions => {
      //botlog.info(s"Checking mentions")
      checkPalabrasMentions()
    }
  }

  var lastCheckedTweet: Option[Long] = None

  def updateIfLarger(l1: Option[Long],l2: Option[Long]) = {
    if(l1.isEmpty){
      l2
    }else if(l2.isEmpty){
      l1
    }else{
      if(l1.get < l2.get){
        l2
      }else{
        l1
      }
    }
  }

  def checkPalabrasMentions() = {
    palabrasTwitterClient.mentionsTimeline(since_id = lastCheckedTweet).foreach{ r =>
      r.data.foreach{ tweet =>
        botlog.info(s"Analyzing: ${tweet.text}")
        val words = tweet.text.split(" ")
        val wordToAnalyze = words.find(w => !w.startsWith("@") && !w.startsWith("#"))
        wordToAnalyze.foreach{ s =>
          tweet.user.foreach{u =>
            val chain = getWordChain(s,TwitterPalabras.palabras).mkString("\n")
            val txt = s"@${u.screen_name}\n$chain"
            palabrasTwitterClient.createTweet(txt,in_reply_to_status_id = Some(tweet.id))
          }
        }
      }
      val newLastChecked = Try{Some(r.data.map(_.id).max)}.getOrElse(None)
      lastCheckedTweet = updateIfLarger(lastCheckedTweet,newLastChecked)
    }
  }

  def createPalabrasDailyTweet() = {
    val wordChain = getNewChain()
    val wordChainTxt = wordChain.mkString("\n")
    palabrasTwitterClient.createTweet(wordChainTxt, latitude = None, longitude = None)
  }

}
