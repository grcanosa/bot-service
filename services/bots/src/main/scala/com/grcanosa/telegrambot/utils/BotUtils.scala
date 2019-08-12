package com.grcanosa.telegrambot.utils

import java.time.{LocalDateTime, ZoneOffset}

import com.vdurmont.emoji.EmojiParser
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.util.{Random, Try}

object BotUtils {

  val BOTLOG  = LoggerFactory.getLogger("botloggger")

  val random = new Random(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))

  implicit class RandomFromList2[T](li: Seq[T]) {

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
