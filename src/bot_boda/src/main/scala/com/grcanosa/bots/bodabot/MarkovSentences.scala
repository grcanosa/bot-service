package com.grcanosa.bots.bodabot

import com.grcanosa.bots.bodabot.QuijoteApp.{CHAIN_SIZE, MARKOV_MAP}

import scala.collection.mutable

object MarkovSentences {

}


class MarkovSentences(val CHAIN_SIZE: Int, val normalize: String => String) {

  val MARKOV_MAP:mutable.Map[Seq[String], mutable.Map[String, Int]] = new mutable.HashMap()
  val STARTWORDS_MAP:mutable.Map[Seq[String], Int] = new mutable.HashMap()

  def adjustProbabilities(sentence:String):Unit = {
    val segments = sentence.split(" ").+:("").:+("").sliding(CHAIN_SIZE + 1).toList
    for(segment <- segments) {
      //println(s"SEGMENT IS $segment")
      val key = segment.take(CHAIN_SIZE)
      val probs = MARKOV_MAP.getOrElse(key, scala.collection.mutable.Map())
      probs(segment.last) = probs.getOrElse(segment.last, 0) + 1
      MARKOV_MAP(key) = probs
      val startWordProps = STARTWORDS_MAP.getOrElse(key,0)
      STARTWORDS_MAP(key) = startWordProps + 1
    }
  }

  lazy val startWords: List[Seq[String]] = MARKOV_MAP.keys.filter(_.head == "").toList

  import scala.util.Random
  val r = new Random()


  def nextWord(seed:Seq[String]):String = {
    val possible = MARKOV_MAP.getOrElse(seed, List())
    r.shuffle(possible.flatMap(pair => List.fill(pair._2)(pair._1))).head
  }

  import scala.collection.mutable.ArrayBuffer
  def nextSentence():String = {
    val seed = startWords(r.nextInt(startWords.size))
    //println(s"SEED IS $seed")
    val sentence:ArrayBuffer[String] = ArrayBuffer()
    sentence.appendAll(seed)
    while(sentence.last != "") {
      sentence.append(nextWord(sentence.view(sentence.size - CHAIN_SIZE, sentence.size)))
    }
    //println(s"SENTENCE $sentence")
    sentence.view(1, sentence.size - 1).mkString(" ").capitalize
  }

  def nextStartWordWithProbability() = {
    r.shuffle(STARTWORDS_MAP.filter(_._1.head == "").flatMap{ case (e,prob) =>
      List.fill(prob)(e)
    }).head
  }

  def nextSentenceWithStartWordsProbable() = {
    val seed = nextStartWordWithProbability()
    //println(s"SEED IS $seed")
    val sentence:ArrayBuffer[String] = ArrayBuffer()
    sentence.appendAll(seed)
    while(sentence.last != "") {
      sentence.append(nextWord(sentence.view(sentence.size - CHAIN_SIZE, sentence.size)))
    }
    //println(s"SENTENCE $sentence")
    sentence.view(1, sentence.size - 1).mkString(" ").capitalize
  }

}
