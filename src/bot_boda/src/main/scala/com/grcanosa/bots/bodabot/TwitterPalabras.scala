package com.grcanosa.bots.bodabot

import akka.Done
import akka.actor.Actor.Receive
import akka.actor.{ActorRef, Timers}
import com.bot4s.telegram.methods.SendMessage
import com.danielasfregola.twitter4s.TwitterRestClient
import com.grcanosa.bots.bodabot.ExtraerPalabras.{getWordRegexList, path}
import com.grcanosa.bots.bodabot.TwitterMessages.DailyTweet
import com.grcanosa.bots.bodabot.TwitterPalabras.{CheckPalabrasMentions, PalabrasTweet, PublishPalabrasTweetCountry, getWordChain}
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

  def getLongestWordChain(word: String, allWords: List[String]) = {
    val allInterestingWords = allWords.filter(_.length == word.length)
    def getLongestWordChainRec(curr: List[List[String]],maxPreviousLength: Int): List[List[String]] = {
      val newList: List[List[String]] = curr.flatMap{ listW =>
        val lastW = listW.last
        val newWords = getAllWordsFromWord(lastW,listW,allInterestingWords)
        newWords match {
          case Nil => List(listW)
          case l => l.map(w => listW :+ w)
        }
      }
      val maxLength = newList.map(_.size).max
      val newListFilter = newList.filter(_.size == maxLength)
      if(maxLength > maxPreviousLength){
        getLongestWordChainRec(newListFilter,maxLength)
      }else{
        newListFilter
      }
    }
    getLongestWordChainRec(List(List(word)),1)
  }

  def getLongestWordChain2(word: String, allWords: List[String]) = {
    val allInterestingWords = allWords.filter(_.length == word.length)
    def getRec(maxSize: Int,maxLists: List[List[String]], toCheckList: List[List[String]], previousExpansions: Map[String,Seq[String]]): List[List[String]] = {
      if(toCheckList.isEmpty){
        maxLists
      }else {
        println(s"MaxSize is $maxSize, maxLists has size ${maxLists.size} to check ${toCheckList.size}")
        val toExpand = toCheckList.head
        println(s"Expanding ${toExpand.mkString(",")}")
        val lastWord = toExpand.last
        val newWords = getAllWordsFromWordNoExpansion(lastWord, allInterestingWords,previousExpansions)
        val newWordsValid = newWords.filter(w => ! toExpand.contains(w))
        val updatedExpansions = previousExpansions + (lastWord -> newWords)
        if (newWordsValid.isEmpty) {
          getRec(maxSize, maxLists, toCheckList.tail,updatedExpansions)
        } else {
          val newSize = toExpand.size + 1
          val newLists = newWordsValid.map(w => toExpand :+ w).toList
          if (newSize > maxSize) {
            getRec(newSize, newLists, newLists ++ toCheckList.tail,updatedExpansions)
          } else if (newSize == maxSize) {
            getRec(newSize, newLists ++ maxLists, newLists ++ toCheckList.tail,updatedExpansions)
          } else {
            getRec(maxSize, maxLists, newLists ++ toCheckList.tail,updatedExpansions)
          }
        }
      }
    }
    Thread.sleep(5)
    getRec(1,List(List(word)),List(List(word)),Map.empty[String,Seq[String]])

  }

  def getAllWordsFromWordNoExpansion(word: String,allWords: List[String], previousExpansions: Map[String,Seq[String]]): Seq[String] = {
    previousExpansions.get(word) match {
      case Some(l) => l
      case None => getAllWordsFromWord(word,List(word),allWords)
    }
  }

  def getAllWordsFromWord(word: String,previousWords: List[String],allWords: List[String]): Seq[String] = {
    val regexList = getWordRegexList(word)
    regexList.flatMap{ reg =>
      allWords.filter{ w =>
        w.length == word.length &&
        !previousWords.contains(w) &&
          reg.findFirstIn(w).isDefined
      }
    }
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

  case class PublishPalabrasTweetCountry(msg: String)

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

  onCommand("/newyear"){ implicit msg =>
    allowedUser(Some("msg")) { uH =>
      isAdmin{_ =>
        val m = java.time.LocalDateTime.now().getMinute
        botlog.info(s"Manually checking with minute $m")
        publishCountries(Some(m))
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
    case PublishPalabrasTweetCountry(msg) => {
      palabrasTwitterClient.createTweet(msg, latitude = None, longitude = None).onComplete{
        case Success(t) => logger.info(s"Published $msg")
        case Failure(e) => logger.error(s"Error publishing $msg",e)
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

  def getNewChainMinSize(minSize: Int): List[String] = {
    val ch = getNewChain()
    if(ch.size >= minSize){
      ch
    }else{
      getNewChainMinSize(minSize)
    }
  }

  def createPalabrasDailyTweet() = {
    val wordChain = getNewChainMinSize(3)
    val wordChainTxt = wordChain.mkString("\n")
    palabrasTwitterClient.createTweet(wordChainTxt, latitude = None, longitude = None)
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

  def publishCountries(min: Option[Int]) = {
    val toPublish = Countries.getMessagesToPublish("Happy New Year to ",None,min)
    toPublish.foreach(botlog.info(_))
    if(toPublish.nonEmpty){
      toPublish.foreach{ msg =>
        selfActor ! PublishPalabrasTweetCountry(msg)
      }
    }

    val toPublish2 = Countries.getMessagesToPublish("30 minutes to New Year in ",Some(30),min)
    toPublish2.foreach(botlog.info(_))
    if(toPublish2.nonEmpty){
      toPublish2.foreach{ msg =>
        selfActor ! PublishPalabrasTweetCountry(msg)
      }
    }
  }


  val futCountries: Future[Done] = akka.stream.scaladsl.Source.tick(5 seconds, 30 seconds,"msg").runForeach { _ => {
    botlog.info(s"Checking to publish")
    publishCountries(None)
  }
  }

  futCountries.onComplete{
    case Success(_) => botlog.info(s"COMPLETED OK")
    case Failure(e) => botlog.error(s"COMPLETED ERROR",e)
  }






}
