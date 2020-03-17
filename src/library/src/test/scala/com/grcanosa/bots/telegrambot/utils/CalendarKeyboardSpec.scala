package com.grcanosa.bots.telegrambot.utils

import java.time.LocalDate
import java.util.{Calendar, GregorianCalendar}

import com.grcanosa.telegrambot.utils.CalendarKeyboard
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CalendarKeyboardSpec extends AnyWordSpec with Matchers with CalendarKeyboard{

//  val cal = createCalendar(Some(2020),Some(3))
//
//  cal.inlineKeyboard.foreach{ r =>
//    r.foreach{ b =>
//      print(b.text)
//    }
//    println("")
//
//  }

  val march2020 = LocalDate.of(2020,3,1)
  val march2020w2 = LocalDate.of(2020,3,2)

  "getCalendarYearMonthButton" should {

    "return correct button for specific date" in {
      getYearMonthButton(march2020).text shouldBe "MARCH 2020"
    }
  }

  "getCalendarFirstWeekEmptyButtons" should {
    "return correct number of empty buttons" in {
      getWeekEmptyButtons(march2020).size shouldBe 6
      getWeekEmptyButtons(march2020w2).size shouldBe 0
    }
  }

  "getMonthButtons" should {
    "return correct number of buttons" in {
      getMonthButtons(march2020,2020,3).size shouldBe 31
    }
    "return the correct texts in buttons" in {
      getMonthButtons(march2020,2020,3).head.text shouldBe "1"
      getMonthButtons(march2020,2020,3).last.text shouldBe "31"
    }
  }

}
