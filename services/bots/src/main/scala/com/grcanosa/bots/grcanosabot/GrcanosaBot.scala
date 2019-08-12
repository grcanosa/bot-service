package com.grcanosa.bots.grcanosabot

import java.time.LocalDateTime

import akka.actor.{Actor, ActorRef, Props}
import akka.stream.scaladsl.Source
import com.bot4s.telegram.api.AkkaDefaults
import com.bot4s.telegram.methods.SendMessage
import GrcanosaBot.{DimeAlgoBonito, DimeAlgoRealmenteBonito, StartDayMessage}
import com.grcanosa.telegrambot.bot.BotWithAdmin
import com.grcanosa.telegrambot.dao.{BotDao, BotUserDao, InteractionDao}
import com.grcanosa.telegrambot.dao.mongo.{BotUserMongoDao, InteractionMongoDao}
import com.grcanosa.telegrambot.model.BotUser

import scala.concurrent.duration._


object GrcanosaBot extends AkkaDefaults{
  case object DimeAlgoBonito
  case object StartDayMessage
  case object DimeAlgoRealmenteBonito

  import com.grcanosa.bots.grcanosabot.utils.GrcanosaBotUtils._

  val token = configGrcanosa.getString("bot.token")
  val adminId = configGrcanosa.getLong("bot.adminId")

  val mongoHost = configGrcanosa.getString("mongo.host")
  val mongoPort = configGrcanosa.getInt("mongo.port")
  val mongoDatabaseName = configGrcanosa.getString("bot.mongo.databasename")

  implicit val ec = system.dispatcher

  val mongoUserDao = new BotUserMongoDao(mongoHost,mongoPort,mongoDatabaseName)

  val mongoInteractionDao = new InteractionMongoDao(mongoHost, mongoPort, mongoDatabaseName)

  implicit object GrupoDao extends BotDao{
    override def botUserDao: BotUserDao = mongoUserDao

    override def interactionDao: InteractionDao = mongoInteractionDao
  }


  val bot = new GrcanosaBot(token,adminId)
}


class GrcanosaBot(override val token: String,override val adminId: Long)
                 (implicit botDao: BotDao)
  extends BotWithAdmin(token,adminId)
with GrcanosaFrases {

  import GrcanosaBotData._

  val grcanosaBotActor = system.actorOf(Props(new GrcanosaBotActor()))


  Source.tick(0 seconds, 1 minute,"msg").runForeach{ _ => {
      val now = LocalDateTime.now()
      if(now.getHour == 9 && now.getMinute == 0){
        grcanosaBotActor ! StartDayMessage
      }
    }
  }


  onCommand("/dimealgobonito"){ implicit msg =>
    allowedUser(Some("dimealgobonito")) { uH =>
      uH.handler ! DimeAlgoBonito
    }
  }

  onCommand("/dimealgorealmentebonito"){ implicit msg =>
    allowedUser(Some("dimealgorealmentebonito")) { uH =>
      uH.handler ! DimeAlgoRealmenteBonito
    }
  }





  class GrcanosaBotActor extends Actor{
    def receive = {
      case StartDayMessage => {
        val sday = getStartDayMessage
        botActor ! SendMessage(adminId, sday)
        botActor ! SendMessage(saraId, sday)
      }
      case _ =>
    }
  }

  class GrcanosaBotUserActor(val botUser: BotUser) extends Actor{

    var dimeAlgoBonitoCount: Int = 0

    override def receive = {
      case DimeAlgoBonito => {
        dimeAlgoBonitoCount match {
          case c if c % 3 == 0 => botActor ! SendMessage(botUser.id,noSeasPresumidoText(botUser.name))
          case _ => botActor ! SendMessage(botUser.id,getAlgoBonito)
        }
      }
      case DimeAlgoRealmenteBonito => {
        botUser.id match {
          case id if id == saraId => botActor ! SendMessage(botUser.id,getAlgoRealmenteBonito)
          case _ => botActor ! SendMessage(botUser.id,cosasRealmenteBonitasSoloUnaPersonaText)
        }
      }
      case _ =>
    }
  }

  override def createNewUserActor(botUser: BotUser): ActorRef = {
    system.actorOf(Props(new GrcanosaBotUserActor(botUser)),s"actor_${botUser.id}")
  }

  override def userNotAllowedResponse(name: String): String = notAllowedText

  override def userRequestPermissionResponse(name: String) = requestingPermissionText

  override def startCmdResponse(name: String): String = startText

  override def helpCmdResponse(name: String) = helpText
}
