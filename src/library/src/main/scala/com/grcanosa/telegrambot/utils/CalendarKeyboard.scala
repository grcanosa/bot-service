package com.grcanosa.telegrambot.utils

import java.time.LocalDate
import com.bot4s.telegram.models.{InlineKeyboardButton, InlineKeyboardMarkup}

trait CalendarKeyboard {

  lazy val KEYBOARD_TAG = "CALENDAR_KEYBOARD"

  lazy val IGNORE_ACTION = "IGNORE"
  lazy val CHANGE_MONTH = "CHANGE_MONTH"
  lazy val DAY_ACTION = "DAY"

  def createCallbackData(action: String,year:String,month: String, day: String) = {
    Some(Seq(KEYBOARD_TAG,action,year,month,day).mkString(";"))
  }

  def separateCallbackData(data: String) = {
    data.split(";").toList match {
      case action :: year :: month :: day :: Nil => (action,year,month,day)
      case _ => (IGNORE_ACTION,"Y","M","D")
    }
  }

  lazy val ignoreCallbakData: Option[String] = createCallbackData(IGNORE_ACTION,"Y","M","D")

  lazy val emptyButton: InlineKeyboardButton = InlineKeyboardButton("X",ignoreCallbakData)


  lazy val weekButtons: Seq[InlineKeyboardButton] =
    Seq( "Mo","Tu","We","Th","Fr","Sa","Su")
      .map(s => InlineKeyboardButton(s,ignoreCallbakData))



  def createCalendar(yearO: Option[Int]=None, monthO: Option[Int]=None): InlineKeyboardMarkup = {
    val year: Int = yearO.getOrElse(java.time.LocalDateTime.now().getYear)
    val month: Int = monthO.getOrElse(java.time.LocalDateTime.now().getMonthValue)
    createCalendar(year,month)
  }

  def getYearMonthButton(date: LocalDate): InlineKeyboardButton = {
      InlineKeyboardButton(
        s"${date.getMonth.toString} ${date.getYear}",ignoreCallbakData)
  }

  def getWeekEmptyButtons(date: LocalDate): Seq[InlineKeyboardButton] = {
    val dayOfWeek = date.getDayOfWeek.getValue
    1 until dayOfWeek map (_ => emptyButton)
  }

  def getMonthButtons(date: LocalDate, year: Int, month: Int): Seq[InlineKeyboardButton] = {
    val maxDayOfMonth = date.lengthOfMonth()
    1 to maxDayOfMonth map { d =>
      InlineKeyboardButton(s"$d",createCallbackData(DAY_ACTION,s"$year",s"$month",s"$d"))
    }
  }

  def getPrevNextButtons(year: Int, month: Int): Seq[InlineKeyboardButton] = {
    val nextMonthButton: InlineKeyboardButton = month match {
      case 12 => InlineKeyboardButton(">",createCallbackData(CHANGE_MONTH,s"${year+1}","1","D"))
      case _ => InlineKeyboardButton(">",createCallbackData(CHANGE_MONTH,s"${year}",s"${month+1}","D"))
    }

    val prevMonthButton: InlineKeyboardButton = month match {
      case 1 =>InlineKeyboardButton("<",createCallbackData(CHANGE_MONTH,s"${year-1}","12","D"))
      case _ => InlineKeyboardButton("<",createCallbackData(CHANGE_MONTH,s"$year",s"${month-1}","D"))
    }

    Seq(
      prevMonthButton
      , emptyButton
      , nextMonthButton
    )
  }

  def createCalendar(year: Int, month: Int): InlineKeyboardMarkup = {

    val date = LocalDate.of(year,month,1)

    val join = getWeekEmptyButtons(date) ++ getMonthButtons(date,year,month)

    val grouped = join.grouped(7).toSeq

    val allButLastWeek: Seq[Seq[InlineKeyboardButton]] = grouped.init

    val lastWeek = grouped.last.size match {
      case 7 => grouped.last
      case n => grouped.last ++ (0 to 7-n).map(_ => emptyButton)
    }

    //Keep all but last week
    val calendarKeys: Seq[Seq[InlineKeyboardButton]] =
      Seq(Seq(getYearMonthButton(date))) ++
      Seq(weekButtons) ++
      allButLastWeek ++
      Seq(lastWeek) ++
      Seq(getPrevNextButtons(year,month))

    InlineKeyboardMarkup(calendarKeys)
  }

  def processCallbackData(data: String) = {
    separateCallbackData(data) match {
      case (IGNORE_ACTION,_,_,_) => (None,None)
      case (CHANGE_MONTH,y,m,_) => (Some(createCalendar(y.toInt,m.toInt)),None)
      case (DAY_ACTION,y,m,d) => (None,Some(LocalDate.of(y.toInt,m.toInt,d.toInt)))
    }
  }

}

//
//
//#!/usr/bin/env python3
//#
//# A library that allows to create an inline calendar keyboard.
//# grcanosa https://github.com/grcanosa
//#
//"""
//Base methods for calendar keyboard creation and processing.
//"""
//
//
//from telegram import InlineKeyboardButton, InlineKeyboardMarkup,ReplyKeyboardRemove
//import datetime
//import calendar
//
//def create_callback_data(action,year,month,day):
//""" Create the callback data associated to each button"""
//return ";".join([action,str(year),str(month),str(day)])
//
//def separate_callback_data(data):
//""" Separate the callback data"""
//return data.split(";")
//
//
//def create_calendar(year=None,month=None):
//"""
//Create an inline keyboard with the provided year and month
//:param int year: Year to use in the calendar, if None the current year is used.
//:param int month: Month to use in the calendar, if None the current month is used.
//:return: Returns the InlineKeyboardMarkup object with the calendar.
//"""
//now = datetime.datetime.now()
//if year == None: year = now.year
//if month == None: month = now.month
//data_ignore = create_callback_data("IGNORE", year, month, 0)
//keyboard = []
//#First row - Month and Year
//row=[]
//row.append(InlineKeyboardButton(calendar.month_name[month]+" "+str(year),callback_data=data_ignore))
//keyboard.append(row)
//#Second row - Week Days
//row=[]
//for day in ["Mo","Tu","We","Th","Fr","Sa","Su"]:
//row.append(InlineKeyboardButton(day,callback_data=data_ignore))
//keyboard.append(row)
//
//my_calendar = calendar.monthcalendar(year, month)
//for week in my_calendar:
//row=[]
//for day in week:
//if(day==0):
//row.append(InlineKeyboardButton(" ",callback_data=data_ignore))
//else:
//row.append(InlineKeyboardButton(str(day),callback_data=create_callback_data("DAY",year,month,day)))
//keyboard.append(row)
//#Last row - Buttons
//row=[]
//row.append(InlineKeyboardButton("<",callback_data=create_callback_data("PREV-MONTH",year,month,day)))
//row.append(InlineKeyboardButton(" ",callback_data=data_ignore))
//row.append(InlineKeyboardButton(">",callback_data=create_callback_data("NEXT-MONTH",year,month,day)))
//keyboard.append(row)
//
//return InlineKeyboardMarkup(keyboard)
//
//
//def process_calendar_selection(bot,update):
//"""
//Process the callback_query. This method generates a new calendar if forward or
//backward is pressed. This method should be called inside a CallbackQueryHandler.
//:param telegram.Bot bot: The bot, as provided by the CallbackQueryHandler
//:param telegram.Update update: The update, as provided by the CallbackQueryHandler
//:return: Returns a tuple (Boolean,datetime.datetime), indicating if a date is selected
//            and returning the date if so.
//"""
//ret_data = (False,None)
//query = update.callback_query
//(action,year,month,day) = separate_callback_data(query.data)
//curr = datetime.datetime(int(year), int(month), 1)
//if action == "IGNORE":
//bot.answer_callback_query(callback_query_id= query.id)
//elif action == "DAY":
//bot.edit_message_text(text=query.message.text,
//chat_id=query.message.chat_id,
//message_id=query.message.message_id
//)
//ret_data = True,datetime.datetime(int(year),int(month),int(day))
//elif action == "PREV-MONTH":
//pre = curr - datetime.timedelta(days=1)
//bot.edit_message_text(text=query.message.text,
//chat_id=query.message.chat_id,
//message_id=query.message.message_id,
//reply_markup=create_calendar(int(pre.year),int(pre.month)))
//elif action == "NEXT-MONTH":
//ne = curr + datetime.timedelta(days=31)
//bot.edit_message_text(text=query.message.text,
//chat_id=query.message.chat_id,
//message_id=query.message.message_id,
//reply_markup=create_calendar(int(ne.year),int(ne.month)))
//else:
//bot.answer_callback_query(callback_query_id= query.id,text="Something went wrong!")
//# UNKNOWN
//return ret_data
