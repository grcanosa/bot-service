package com.grcanosa.telegrambot.bot.user

import akka.actor.ActorRef
import com.bot4s.telegram.models.User
import com.grcanosa.telegrambot.dao.BotDao
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.model.BotUser.PERMISSION_ALLOWED
import com.grcanosa.telegrambot.utils.LazyBotLogging

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}



trait UserRegistry extends BotDao with LazyBotLogging{

  implicit def executionContext: ExecutionContext

  def createNewUserActor(botUser: BotUser): ActorRef



  val userHandlers = collection.mutable.Map[Long, UserHandler]()

  def permittedUserHandlers =
    userHandlers.filter(_._2.user.permission == PERMISSION_ALLOWED).values.toSeq

  botUserDao.getUsers().onComplete{
    case Failure(exception) => botlog.error(s"Error getting users ${exception.toString}")
    case Success(seqU) => seqU match {
      case Seq() => botlog.info("Empty user list retrived from database")
      case _ => seqU.foreach{ bu =>
        botlog.info(s"Loading user: ${bu.id.toString}, ${bu.name} with permission: ${bu.permission.toString}")
        userHandlers.update(bu.id,UserHandler(bu,createNewUserActor(bu)))
      }
    }
  }

  def getUser(uid: Long) = {
    userHandlers.get(uid)
  }



  def getUser(user: User) = {
    userHandlers.getOrElseUpdate(user.id,{
      val bu = BotUser.fromUser(user)
      botlog.info(s"Inserting user into database: ${bu.name}")
      val fut = botUserDao.insertUser(bu).map{
        case true => {
          botlog.info("User inserted OK")
          UserHandler(bu,createNewUserActor(bu))
        }
        case false => {
          botlog.info("Error inserting User")
          UserHandler(bu, null)
        }
      }
      Await.result(fut, 10 seconds)
      fut.value.get.get
    })
  }



  def updateUser(userH: UserHandler) = {
    botlog.info(s"Updating user ${userH.user.name}")
    userHandlers.update(userH.user.id,userH)
    botUserDao.updateUser(userH.user)
  }
}
