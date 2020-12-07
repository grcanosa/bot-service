package com.grcanosa.bots.bodabot

import java.nio.file.Paths

import akka.stream.scaladsl.{FileIO, Framing, Keep, RunnableGraph, Sink}
import akka.util.ByteString
import com.bot4s.telegram.api.{AkkaDefaults, AkkaImplicits}

import scala.collection.immutable
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration


object QuijoteApp extends App with AkkaDefaults{

  val sanchoSentences = "(?i)(?:(?:\\s+)?|[-?!])([^.?!]*([^\\s]?dijo sancho|respondio. sancho[^\\s]?)[^.!?]*[.!?])".r
  val sanchoSentences2 = "(?i)(?:(?:\\s+)?|[-?!])([^.?!]*([,-]?(?:dijo|respondio.) sancho(?:\\spanza)?[,-]?)[^.!?]*[.!?])".r

  val s =FileIO.fromPath(Paths.get("quijote.txt"))
    .via(Framing.delimiter(ByteString("\n"), 100000, true).map(_.utf8String))
    .mapConcat { s =>
        sanchoSentences2.findAllMatchIn(s).map { m =>
          //println(m.groupCount,m.group(1),m.group(2))
          m.group(1).replace(m.group(2),"")
        }.toList
    }
    .toMat(Sink.seq)(Keep.right)

  val f = s.run()

  Await.result(f,Duration.Inf)

  val strings = f.value.get.get

  println(s"STRINGS SIZE IS ${strings.size}")

  import scala.collection.mutable

  val MARKOV_MAP:mutable.Map[Seq[String], mutable.Map[String, Int]] = new mutable.HashMap()
  val CHAIN_SIZE = 2

  def adjustProbabilities(sentence:String):Unit = {
    val segments = sentence.split(" ").+:("").:+("").sliding(CHAIN_SIZE + 1).toList
    for(segment <- segments) {
      //println(s"SEGMENT IS $segment")
      val key = segment.take(CHAIN_SIZE)
      val probs = MARKOV_MAP.getOrElse(key, scala.collection.mutable.Map())
      probs(segment.last) = probs.getOrElse(segment.last, 0) + 1
      MARKOV_MAP(key) = probs
    }
  }

  def normalize(line: String): String = {
    line.stripLineEnd
      //.toLowerCase
      .filterNot("\\.-,\";:&" contains _)
  }

  import scala.io.Source
  val filePath = "/Users/phillip/Documents/Programming/markov-tweets/src/main/resources/shakespeare_corpus.txt"

  strings
    .map(normalize)
    .map{ s =>
      println(s"STRING IS $s")
      s.trim
    }
    .foreach(s => adjustProbabilities(s))

  val startWords = MARKOV_MAP.keys.filter(_.head == "").toList


  //println(s"START WORDS $startWords")

  import scala.util.Random
  val r = new Random()

  def nextWord(seed:Seq[String]):String = {
    val possible = MARKOV_MAP.getOrElse(seed, List())
    r.shuffle(possible.flatMap(pair => List.fill(pair._2)(pair._1))).head
  }

  import scala.collection.mutable.ArrayBuffer
  def nextSentence():String = {
    val seed = startWords(r.nextInt(startWords.size))
    println(s"SEED IS $seed")
    val sentence:ArrayBuffer[String] = ArrayBuffer()
    sentence.appendAll(seed)
    while(sentence.last != "") {
      sentence.append(nextWord(sentence.view(sentence.size - CHAIN_SIZE, sentence.size)))
    }
    println(s"SENTENCE $sentence")
    sentence.view(1, sentence.size - 1).mkString(" ").capitalize
  }

  val ss = (0 until 14).map(_ => nextSentence())

  ss.foreach{ sss =>
    println(sss.length,sss)
  }





  system.terminate()


}
