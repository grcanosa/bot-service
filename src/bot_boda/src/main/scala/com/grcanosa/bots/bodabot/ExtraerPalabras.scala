package com.grcanosa.bots.bodabot

import com.bot4s.telegram.api.AkkaDefaults

import java.io.{File, PrintWriter}
import java.text.Normalizer
import scala.io.Source

object ExtraerPalabras extends App with AkkaDefaults{


  val letras = Seq("a","b","c","d","e","f","g","h","i","j","k"
    ,"l","m","n","ñ","o","p","q","r","s","t","u","v","w","x","y","z")

  val path = (letra: String) => s"palabras/$letra.txt"

  val numbers = "[234567890]".r

  val palabrasSet = letras.flatMap{ l =>
    Source.fromResource(path(l)).getLines
  }
    .map(_.trim)
    .filter(!_.contains(" "))
    .filter(!_.startsWith("-"))
    .map{w => w.filterNot(_.isDigit)}
    .filter(w => numbers.findFirstIn(w).isEmpty)
    .filter(_.length >=3)
    .flatMap{ w =>
      if(w.contains(",")){
        val spli = w.split(",")
        val w1 = spli.head
        val w2end = spli.tail.head.replace(" ","")
        if(w2end.isEmpty){
          println(s"SPECIAL CASE $w")
          List()
        }else if(w1.endsWith(w2end.head.toString)){
          //println(s"Splitting $w that ends with ${w2end.head.toString} returning $w1 and ${w1+w2end.tail}")
          List(w1,w1+w2end.tail)
        }else if(w2end.length == 1){
          //println(s"Splitting $w and w2end is $w2end getting $w1 and ${w1.reverse.tail.reverse.mkString+w2end}")
          List(w1,w1.reverse.tail.reverse.mkString+w2end)
        }else{
          val w1spli = w1.reverse.split(w2end.head)
          //println(s"Splitting $w that w1 is $w1 splitted by ${w2end.head} returning $w1 and ${w1spli.tail.mkString(w2end.head.toString).reverse+w2end}")
          List(w1,w1spli.tail.mkString(w2end.head.toString).reverse+w2end)
        }
      }else{
        List(w)
      }
    }
    .map{ w =>
      if(w.endsWith("óna")){
        w.replace("óna","ona")
      }else{
        w
      }
    }.toSet

  val palabras = palabrasSet.toList.sortWith{ case (s1,s2) =>
    Normalizer.normalize(s1.toLowerCase,Normalizer.Form.NFD) <
      Normalizer.normalize(s2.toLowerCase,Normalizer.Form.NFD)
  }

  // palabras.foreach(println)
  val pw = new PrintWriter(new File("palabras.txt" ))
  palabras.foreach{ w =>
    pw.write(s"$w\n")
  }

  pw.close()


  import com.grcanosa.telegrambot.utils.BotUtils._


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


//  val p = palabras.chooseRandom()
//
//
//  val allSubs = (0 until p.length).map{ i =>
//    substituteLetter(p,i,allLeters)
//  }
//
//  println(p)
//  //allSubs.foreach(println)
//
//  val allSubsFromEndingRegex = allSubs.reverse.map(_.r)
//
//  val wNew = allSubsFromEndingRegex.foldLeft(Option.empty[String]){ case (opS,regex) =>
//    if(opS.isDefined) opS
//    else {
//      palabras.find(w => regex.findFirstIn(w).isDefined)
//    }
//  }
//
//  println(wNew)

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

  (0 until 30).map{ _ =>
    val w = palabras.chooseRandom()
    val wordChain = getWordChain(w,palabras)
    println("*******")
    wordChain.foreach(println)
  }


  system.terminate()



}
