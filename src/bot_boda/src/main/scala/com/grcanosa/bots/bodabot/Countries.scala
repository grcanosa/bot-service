package com.grcanosa.bots.bodabot

import java.time.{LocalDateTime, ZoneId}
import java.util.Locale
import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.io.Source


object Countries {

  val countryCodes = Locale.getISOCountries().toList
  val countries1 = countryCodes.map{ code =>
    val obj = new Locale("", code)
    val name = obj.getDisplayCountry
    (code,name)
  }

  val numberRegex = "[0-9]".r

  val zoneIds = ZoneId.getAvailableZoneIds
    .asScala
    .toList
    .filter(_.contains("/"))
    .filter(!_.contains("+"))
    .filter(s => numberRegex.findFirstIn(s).isEmpty)
    .map(z => ZoneId.of(z))


  val d = Source.fromResource("countries.json").getLines().mkString("\n")


  import scala.util.parsing.json._

  val json:Option[Any] = JSON.parseFull(d)
  val map:Map[String,Any] = json.get.asInstanceOf[Map[String, Any]]


  val countries = map("countries").asInstanceOf[Map[String,String]]

  val timezones = map("timezones").asInstanceOf[Map[String,Map[String,Any]]].toList


  def findTimezones(code: String): List[String] = {
    timezones.filter{ case (timezone,m) =>
      m.contains("c") && m("c").asInstanceOf[String]== code
    }.map{ case (timezone,m) =>
      timezone
    }
  }

  case class CountryInfo(name: String, code: String,timezones: List[String])

  val countriesInfo: List[CountryInfo] = map("countries").asInstanceOf[Map[String,String]].map{ case (code, name) =>
    CountryInfo(name,code,findTimezones(code))
  }.toList



  def getCountriesAndTimezonesToPublish(preNewYearMinute: Option[Int]) = {
    countriesInfo.flatMap{ cInfo =>
      val isNewYearList = cInfo.timezones.map(t => (t,isNewYear(ZoneId.of(t),preNewYearMinute)))
      if(isNewYearList.exists(_._2)){
        Some(CountryInfo(cInfo.name,cInfo.code,isNewYearList.filter(_._2).map(_._1)))
      }else{
        None
      }
    }
  }


  def isNewYear(zoneId: ZoneId, preNewYearMinute: Option[Int]): Boolean = {
    val d = LocalDateTime.now(zoneId)
    if (preNewYearMinute.isDefined) {
      d.getDayOfMonth == 31 &&
        d.getHour == 23 &&
        d.getMinute == preNewYearMinute.get &&
        (d.getSecond >= 0 || d.getSecond <= 30)
    } else {
    d.getDayOfYear == 1 &&
      d.getHour == 0 &&
      d.getMinute == 0 &&
      (d.getSecond >= 0 || d.getSecond <= 30)
  }
  }

  val maxNumberofChars = 280

  def convertToMessages(prefix: String,l: List[String]): List[String] = {
    l.foldLeft(List.empty[String]){ case (join,add) =>
      if(join.nonEmpty && join.head.length + add.length > maxNumberofChars){
        add :: join
      }else if(join.nonEmpty){
        join.head + s", ${add}" :: join.tail
      }else{
        List(s"$prefix "+add)
      }
    }
  }



  def getMessagesToPublish(prefix: String, preNewYearMinute: Option[Int]): List[String] = {
    val countriesToPublish = getCountriesAndTimezonesToPublish(preNewYearMinute)
    if(countriesToPublish.nonEmpty){
      val countriesNewYear = countriesToPublish.map{ cInfo =>
        val cities = cInfo.timezones.filter(_.contains("/")).map((s => s.split("/").last))
        s"${cInfo.name} ${cities.mkString("(",",",")")}"
      }
      convertToMessages(prefix,countriesNewYear)
    }else{
      List.empty[String]
    }
  }

}
