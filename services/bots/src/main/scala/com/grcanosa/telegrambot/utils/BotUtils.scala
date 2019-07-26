package com.grcanosa.telegrambot.utils

import com.vdurmont.emoji.EmojiParser
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.util.Try

object BotUtils {

  val BOTLOG  = LoggerFactory.getLogger("botloggger")


  implicit class RandomFromList2[T](li: Seq[T]) {
    import scala.util.Random
    val random = new Random
    def chooseRandom(): Option[T] = li.length match {
      case 0 => None
      case _ => Some(li(random.nextInt(li.length)))
    }
  }

  implicit class StringHelper(s: String){
    def emojize: String = EmojiParser.parseToUnicode(s)

    def getLinesAsSeq = {
      Try {
        val buff = Source.fromFile(s)
        val lines = buff.getLines().toSeq.map(_.emojize)
        buff.close
        lines
      }.recover{
        case e => Seq()
      }.get
    }

  }



}
