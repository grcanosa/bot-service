package com.grcanosa.telegrambot.utils

import com.vdurmont.emoji.EmojiParser

import scala.io.{BufferedSource, Source}
import scala.util.Try

trait StringUtils {
  implicit class StringHelper(s: String){
    def emojize: String = EmojiParser.parseToUnicode(s)

    def botmessage: String = EmojiParser.parseToUnicode(":robot_face::speech_balloon: "+s)

    def getFileLinesAsSeq = {
      Try {
        val buff = Source.fromFile(s)
        val lines = buff.getLines().toList.map(_.emojize)
        buff.close
        lines
      }.recover{
        case e => Seq()
      }.get
    }

    def getResourceFileLinesAsSeq() = {
      Try {
        val buff: BufferedSource = Source.fromResource(s)
        val lines= buff.getLines().toList.map(_.emojize)
        buff.close
        lines
      }.recover{
        case e => Seq()
      }.get
    }
  }
}
