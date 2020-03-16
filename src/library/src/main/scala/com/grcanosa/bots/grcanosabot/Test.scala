package com.grcanosa.bots.grcanosabot

import akka.actor.ActorSystem
import akka.http.javadsl.model.headers.HttpCredentials
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpMethods, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import com.grcanosa.telegrambot.utils.BotUtils.BOTLOG

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Test extends App{

  def homeAssistantToken: String = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIzYjUyZDVkYzRiOTA0MTRhYWEwYzA0ZjU0NmFlYWUxNSIsImlhdCI6MTU2Njc1MTYyOSwiZXhwIjoxODgyMTExNjI5fQ.x7MZVzd8XuGoTc_XEfU-tbfaBwJSapVJS9FqTIfJ-YI"

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher
  val req = HttpRequest(
    HttpMethods.GET,
    uri="http://192.168.1.100:8123/api/config",
    List(
      //akka.http.scaladsl.model.headers.Authorization(HttpCredentials.createOAuth2BearerToken(homeAssistantToken))
      RawHeader("Authorization","Bearer "+homeAssistantToken)
    )
  )
  val http = Http()

  println(15.toDouble.toString)

  def updateTermo() = {
    BOTLOG.info("REQUESTING")
    val a: Future[HttpResponse] = http.singleRequest(req)
    a.onComplete {
      case Success(res) => BOTLOG.info(res.toString())
      case Failure(_)   => BOTLOG.error("something wrong")
    }
    BOTLOG.info(a.toString)
  }

  updateTermo()


}
