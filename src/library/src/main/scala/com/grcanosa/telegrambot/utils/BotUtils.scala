package com.grcanosa.telegrambot.utils

import java.time.{LocalDateTime, ZoneOffset}

import com.vdurmont.emoji.EmojiParser
import org.slf4j.LoggerFactory

import scala.io.{BufferedSource, Source}
import scala.util.{Random, Try}



object BotUtils extends StringUtils {

  //val BOTLOG  = LoggerFactory.getLogger("botloggger")

  val random = new Random(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))

  implicit class RandomFromList2[T](li: Seq[T]) {

    def chooseRandom(): Option[T] = li.length match {
      case 0 => None
      case _ => Some(li(random.nextInt(li.length)))
    }
  }





}
