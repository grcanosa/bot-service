package com.grcanosa.telegrambot.bot.user

import akka.actor.{ActorRef, ActorSystem, Props}
import com.bot4s.telegram.models.User
import com.grcanosa.telegrambot.dao.BotDao
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.model.BotUser.PERMISSION_ALLOWED

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Failure, Success}



trait UserRegistry extends BotDao{

  implicit def executionContext: ExecutionContext

  def createNewUserActor(botUser: BotUser): ActorRef

  import com.grcanosa.telegrambot.utils.BotUtils._


  val userHandlers = collection.mutable.Map[Long, UserHandler]()

  def permittedUserHandlers =
    userHandlers.filter(_._2.user.permission == PERMISSION_ALLOWED).values.toSeq

  botUserDao.getUsers().onComplete{
    case Failure(exception) => BOTLOG.error(s"Error getting users ${exception.toString}")
    case Success(seqU) => seqU match {
      case Seq() => BOTLOG.info("Empty user list retrived from database")
      case _ => seqU.foreach{ bu =>
        BOTLOG.info(s"Loading user: ${bu.id.toString}, ${bu.name} with permission: ${bu.permission.toString}")
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
      BOTLOG.info(s"Inserting user into database: ${bu.name}")
      val fut = botUserDao.insertUser(bu).map{
        case true => {
          BOTLOG.info("User inserted OK")
          UserHandler(bu,createNewUserActor(bu))
        }
        case false => {
          BOTLOG.info("Error inserting User")
          UserHandler(bu, null)
        }
      }
      Await.result(fut, 10 seconds)
      fut.value.get.get
    })
  }



  def updateUser(userH: UserHandler) = {
    BOTLOG.info(s"Updating user ${userH.user.name}")
    userHandlers.update(userH.user.id,userH)
    botUserDao.updateUser(userH.user)
  }
}
