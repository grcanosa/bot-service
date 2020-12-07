package com.grcanosa.bots.bodabot

import org.jsoup.Jsoup
import org.jsoup.select.Elements
import scala.collection.JavaConverters._


object TaskMasterApp extends App{

  val doc = Jsoup.connect("https://task.fandom.com/wiki/Episode_list").get()

  val tables: Elements = doc.select("table")

  val tasks = tables.iterator().asScala.zipWithIndex.flatMap{ case (t,i) =>
    val rows = t.select("tr")
    //println(s"Table $i")
    rows.iterator().asScala.toList.zipWithIndex.flatMap{ case(r,j) =>
      val cols = r.select("td")
      if(cols.size == 7){
        Some(cols.get(1).text())
      }else{
        None
      }
    }
  }.toList
  val tasksLower = tasks.map(_.toLowerCase)
  //tasks.foreach(println)


  val markov = new MarkovSentences(2,identity)

  tasks.foreach{ s =>
   // println(s"ADJUST: $s")
    markov.adjustProbabilities(s)
  }
  println(s"START WORDS")
//  markov.MARKOV_MAP.filter(_._1.head == "").toList.foreach { s =>
//    println(s._1.mkString(","), s._2)
//  }
    markov.STARTWORDS_MAP.filter(_._1.head == "").toList.foreach{s =>
      println(s._1.mkString(","),s._2)
  }
  println("NEW TASKS")
  (0 to 30).foreach{ _ =>
    val s = markov.nextSentence()
    val b = tasksLower.contains(s.toLowerCase)
    println(b,s)
  }
  println("NEW TASKS WITH PROB")
  (0 to 30).foreach{ _ =>
    val s = markov.nextSentenceWithStartWordsProbable()
    val b = tasksLower.contains(s.toLowerCase)
    println(b,s)
  }

  import java.io._
  val pw = new PrintWriter(new File("tasks.txt" ))
  tasks.foreach{t =>
    pw.write(s"$t\n")
  }

  pw.close()


}
