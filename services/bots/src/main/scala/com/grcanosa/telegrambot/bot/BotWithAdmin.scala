package com.grcanosa.telegrambot.bot

import akka.actor.{Actor, Props}
import com.bot4s.telegram.api.{AkkaDefaults, Polling, TelegramBot}
import com.bot4s.telegram.api.declarative.{Commands, Messages}
import com.bot4s.telegram.clients.AkkaHttpClient
import com.bot4s.telegram.methods.{MultipartRequest, SendAnimation, SendAudio, SendMessage, SendPhoto, SendVideo, SendVideoNote, SendVoice}
import com.bot4s.telegram.models.{InputFile, Message}
import com.grcanosa.telegrambot.bot.BotWithAdmin.ForwardMessageTo
import com.grcanosa.telegrambot.bot.user.UserHandler
import com.grcanosa.telegrambot.dao.{BotDao, BotUserDao, InteractionDao}
import com.grcanosa.telegrambot.model.BotUser.{PERMISSION_ALLOWED, PERMISSION_NOT_ALLOWED}


object BotWithAdmin{
  case class ForwardMessageTo(chatId: Long, message: Message)
}








abstract class BotWithAdmin(val token: String
                   , val adminId: Long
                   )(implicit botDao: BotDao)
extends TelegramBot
 with AkkaDefaults
with Polling
with Messages
with BotUsersWithAdmin
with BotResponses
with BotKeyboards
with Commands{

  import com.grcanosa.telegrambot.utils.BotUtils._

  override val client = new AkkaHttpClient(token)
  val botActor = system.actorOf(Props(new BotActor), name = "botActor")

  override def userNotAllowed(userH: UserHandler): Unit = {
    botActor ! SendMessage(userH.user.id,userNotAllowedResponse(userH.user.name))
  }

  override def userRequestPermission(userH: UserHandler): Unit = {
    BOTLOG.info(s"User ${userH.user.id} requests permission")
    botActor ! SendMessage(userH.user.id, userRequestPermissionResponse(userH.user.name))
    val keyboard = permissionKeyboard(userH.user)
    botActor ! SendMessage(adminId,
      s"""User ${userH.user.name} requesting permission
         |/permission ${userH.user.id} ALLOW
         |/permission ${userH.user.id} NOTALLOW
         |""".stripMargin
      ,replyMarkup = Some(keyboard))
  }

  onCommand("/start"){ implicit msg =>
    allowedUser(Some("start")) { uH =>
      reply(startCmdResponse(uH.user.name))
    }
  }

  onCommand("/users") { implicit msg =>
    isAdmin{ _ =>
      val msg = userHandlers.values.toSeq.map{ uh =>
        BOTLOG.info(s"User Handler is $uh")
        s"${uh.user.id};${uh.user.name};${uh.user.permission}"
      }.mkString("\n")
      botActor ! SendMessage(adminId,msg)
    }()
  }

  onCommand("/help"){ implicit msg =>
    allowedUser(Some("help")) { uH =>
      isAdmin { _ =>
        val normalmsg = helpCmdResponse(uH.user.name)
        val realmsg = normalmsg + "\n" + "/users\n/permission"
        reply(realmsg)
      }{ _ =>
        reply(helpCmdResponse(uH.user.name))
      }
    }
  }

  onCommand("/permission"){ implicit msg =>
    isAdmin { _ =>
      withArgs{ args => args.size match {
          case 2 => {
            val uid = args.head.toLong
            args(1) match {
              case "ALLOW" => {
                changeUserPermission(uid,PERMISSION_ALLOWED)
                reply("Allowing user",replyMarkup = Some(removeKeyboard))
                botActor ! SendMessage(uid,permissionGrantedResponse)
              }
              case "NOTALLOW" => {
                changeUserPermission(uid,PERMISSION_NOT_ALLOWED)
                reply("NOT Allowing user",replyMarkup = Some(removeKeyboard))
              }
              case _ => reply(s"Invalid argument 2, ${args(1)}",replyMarkup = Some(removeKeyboard))
            }
          }
          case _ => reply("Invalid command")
        }
      }
    }{ _ =>
      reply("You do not have permission to access this functionality")
    }
  }


  class BotActor extends Actor{


    override def receive = {
      case sm: SendMessage => request(sm)
      case sp: SendPhoto => request(sp)
      case sam: SendAnimation => request(sam)
      case sa: SendAudio => request(sa)
      case sv: SendVideo => request(sv)
      case svn: SendVideoNote => request(svn)
      case sv: SendVoice => request(sv)
      case ForwardMessageTo(cid,msg) => {
        msg.text.foreach(s => self ! SendMessage(cid,s))
        msg.photo.foreach(s => self ! SendPhoto(cid
                              ,InputFile(s.reverse.head.fileId)
                              ,caption=msg.caption)
                        )
        msg.animation.foreach( s => self ! SendAnimation(cid,InputFile(s.fileId),replyToMessageId = None))
        msg.audio.foreach(s => self ! SendAudio(cid,InputFile(s.fileId)))
        msg.video.foreach(s => self ! SendVideo(cid,InputFile(s.fileId),duration=Some(s.duration)))
        msg.videoNote.foreach(s => self ! SendVideoNote(cid,InputFile(s.fileId)))
        msg.voice.foreach(s => self ! SendVoice(cid,InputFile(s.fileId)))
      }
      case _ =>
    }
  }

  override def botUserDao = botDao.botUserDao

  override def interactionDao: InteractionDao = botDao.interactionDao


}
