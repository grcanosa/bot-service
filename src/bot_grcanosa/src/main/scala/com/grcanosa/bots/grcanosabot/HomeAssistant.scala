package com.grcanosa.bots.grcanosabot


import akka.http.scaladsl.model.headers.{BasicHttpCredentials, HttpCredentials, OAuth2BearerToken, RawHeader}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpEntity, HttpHeader, HttpMethod, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import com.grcanosa.telegrambot.bot.BotWithAdmin

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import spray.json._
import DefaultJsonProtocol._
import akka.util.ByteString
import com.grcanosa.telegrambot.utils.LazyBotLogging

trait HomeAssistant extends LazyBotLogging{ this: BotWithAdmin =>

  val http = Http(system)

  def termoValueJson(state: Double) = s"""{
            "entity_id": "input_number.termo_time",
            "value": "${state.toString}"
            }"""

  def homeAssistantToken: String

  val termoStateRequestGet =  HttpRequest(
    HttpMethods.GET,
    uri="http://192.168.10.200:8123/api/states/input_number.termo_time",
    List(
      akka.http.scaladsl.model.headers.Authorization(OAuth2BearerToken(homeAssistantToken))
    )
  )

  def getTermoState () = Http().singleRequest(termoStateRequestGet)
    .flatMap { resp =>
      //BOTLOG.info(s"${resp.toString}")
      resp.entity.toStrict(1 second).map { strict =>
        val str = strict.data.utf8String
        //BOTLOG.info(s"JSON IS: $str")
        //println(str.parseJson.asJsObject.fields)
        str.parseJson.asJsObject.fields("state") match {
          case JsString(v) => v.toDouble
          case _ => 0.toDouble
        }
      }
    }


  def updateValueReq(state: String) = HttpRequest(
    HttpMethods.POST,
    uri="http://192.168.10.200:8123/api/services/input_number/set_value",
    List(
      akka.http.scaladsl.model.headers.Authorization(OAuth2BearerToken(homeAssistantToken))
    )
  ).withEntity(HttpEntity(ContentTypes.`application/json`, state))



  def addTermoMinutes(min: Int): Future[Double] = {
    botlog.info(s"Increasing termo $min minutes")
    getTermoState().flatMap{ st =>
      botlog.info(s"State is $st ")
      val new_time: Double = st + min
      val termo_state = termoValueJson(new_time)
      //BOTLOG.info(s"Termo is $termo_state ")
      val req = updateValueReq(termo_state)
      http.singleRequest(req).flatMap { resp =>
        resp.status match {
          case StatusCodes.OK => getTermoState()
          case _ => Future{-1}
        }
      }
    }
  }




}
