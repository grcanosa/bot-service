package com.grcanosa.bots.rociobot

import akka.actor.{Actor, ActorRef, Props, Timers}
import com.bot4s.telegram.api.declarative.RegexCommands
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Message
import com.grcanosa.telegrambot.bot.BotWithAdmin
import com.grcanosa.telegrambot.dao.mongo.{BotUserMongoDao, InteractionMongoDao}
import com.grcanosa.telegrambot.dao.{BotDao, BotUserDao, InteractionDao}
import com.grcanosa.telegrambot.model.BotUser
import com.grcanosa.telegrambot.model.BotUser.{PERMISSION_ALLOWED, PERMISSION_NOT_SET}
import com.typesafe.config.Config
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext


object Rocio2Bot {

  def apply(config: Config)
           (implicit ec: ExecutionContext): Rocio2Bot = {
    val token = config.getString("bot.token")
    val adminId = config.getLong("bot.adminId")

    val mongoHost = config.getString("mongo.host")
    val mongoPort = config.getInt("mongo.port")
    val mongoDatabaseName = config.getString("bot.mongo.databasename")

    val mongoUserDao = new BotUserMongoDao(mongoHost, mongoPort, mongoDatabaseName)

    val mongoInteractionDao = new InteractionMongoDao(mongoHost, mongoPort, mongoDatabaseName)

    implicit object RocioDao extends BotDao {
      override def botUserDao: BotUserDao = mongoUserDao

      override def interactionDao: InteractionDao = mongoInteractionDao
    }

    val bot = new Rocio2Bot(token, adminId)
    bot
  }

}

class Rocio2Bot(override val token: String
              , override val adminId: Long)
              (implicit botDao: BotDao)
extends BotWithAdmin(token, adminId)
  with RegexCommands
  with RocioBotResponses {

  override val defaultUserPermission: BotUser.BotUserPermission = PERMISSION_ALLOWED

  val selfActor: ActorRef = system.actorOf(Props(new RocioBotActor()))

  object RocioBotUserActor {
    case class RespondMessage(msg: Message)
    case object StartupText
    case object SendQuestion
  }
  import RocioBotUserActor._

  onMessage { implicit msg =>
      allUsers(Some("msg")) { uH => isNotCommand { _ =>
        uH.handler ! RespondMessage(msg)
      }
    }
 }


  class RocioBotActor extends Actor{

    override def receive = {
      case _ =>
    }

  }

  val questions = realQuestions
  val answers = List("A","B","C","D")

  def getQuestionIdx(idx: Int) = {
    questions.lift(idx)
  }


  class RocioBotUserActor(botUser: BotUser) extends Actor with Timers{
    botlog.info(s"Creating actor for user ${botUser.id} ${botUser.name}")

    var currentQuestionIndex = -1
    var currentPointsIdx = 0
    var totalPoints = 0

    self ! StartupText

    def respondQuestionMessage(msg: Message) = {
      botlog.info(s"Responding message ${msg.text} from user ${botUser.name}")
      val q = getQuestionIdx(currentQuestionIndex)
      q.foreach{ question =>
        if (msg.text.isDefined && answers.exists(_ == msg.text.get)) {
          if (msg.text.get == question.solution ) {
            if(currentPointsIdx < question.points.size){
              totalPoints += question.points(currentPointsIdx)
            }
            reply(goodAnswer(totalPoints))(msg)
            timers.startSingleTimer("new_question", SendQuestion, 2 seconds)
          }
          else
          {
            currentPointsIdx += 1
            reply(badAnswer)(msg)
          }
        } else {
          reply(notAnswerResponse)(msg)
        }
      }

    }

    def sendNewQuestion() = {
      botlog.info(s"Sending new question to ${botUser.id}- ${botUser.name}")
      currentQuestionIndex += 1
      currentPointsIdx = 0
      val q = getQuestionIdx(currentQuestionIndex)
      q match {
        case Some(question) => botActor ! SendMessage(botUser.id,question.questionMsg(currentQuestionIndex+1),replyMarkup = Some(answersKeyboard))
        case None => {
          botActor ! SendMessage(botUser.id,finalPointsResponse(totalPoints),replyMarkup = Some(removeKeyboard))
          //botActor ! SendMessage(botUser.id, "No more questions",replyMarkup = Some(removeKeyboard))
          context.become(notQuizzBehaviour)
        }
      }
    }

    def respondNoQuestionMessage(msg: Message) = {
      reply(getUnknownResponse)(msg)
    }

    def quizzBehaviour: Receive = {
      case SendQuestion => sendNewQuestion()
      case RespondMessage(msg) => respondQuestionMessage(msg)
    }

    def notQuizzBehaviour: Receive = {
      case RespondMessage(msg) => respondNoQuestionMessage(msg)
      case StartupText => {
        botActor ! SendMessage(botUser.id,startupText)
        context.become(quizzBehaviour)
        self ! SendQuestion
      }
    }

    override def receive = notQuizzBehaviour


  }

  override def createNewUserActor(botUser: BotUser): ActorRef = {
    botlog.info(s"Creating actor ${botUser.id}-${botUser.name}")
    system.actorOf(Props(new RocioBotUserActor(botUser)), s"actor_${botUser.id}")
  }

  override def startCmdResponse(name: String) = {
    s"¡Hola $name!"
  }

  override def helpCmdResponse(name: String) = {
    s"¡Hola $name!"
  }

}