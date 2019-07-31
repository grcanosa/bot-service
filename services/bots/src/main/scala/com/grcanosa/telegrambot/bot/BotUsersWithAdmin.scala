package com.grcanosa.telegrambot.bot


import com.bot4s.telegram.api.AkkaDefaults
import com.bot4s.telegram.api.declarative.Action
import com.bot4s.telegram.models.Message
import com.grcanosa.telegrambot.bot.user.{UserHandler, UserRegistry}
import com.grcanosa.telegrambot.model.BotUser.{BotUserPermission, PERMISSION_ALLOWED, PERMISSION_NOT_ALLOWED, PERMISSION_NOT_SET}
import com.grcanosa.telegrambot.model.Interaction

trait BotUsersWithAdmin
  extends AkkaDefaults
    with UserRegistry {

  import com.grcanosa.telegrambot.utils.BotUtils._

  def adminId: Long

  def userNotAllowed(userH: UserHandler)

  def userRequestPermission(userH: UserHandler)


  def addInteraction(interaction: Option[String])(implicit msg: Message) = {
    interaction.foreach{ s =>
      interactionDao.insert(Interaction(userFromMessage(msg).id,s))
    }
  }

  def allowedUser(interaction:Option[String])( action: Action[UserHandler] )(implicit msg: Message) = {
    //BOTLOG.info(s"ALLOWED USER CALL: ${msg.text.getOrElse("NOTEXT")}")
    addInteraction(interaction)
    val user = userFromMessage(msg)
    val userH = getUser(user)
    userH.user.permission match {
      case PERMISSION_ALLOWED => action(userH)
      case PERMISSION_NOT_SET => userRequestPermission(userH)
      case PERMISSION_NOT_ALLOWED => userNotAllowed(userH)
      case _ => BOTLOG.error(s"No permission for user ${userH.user.name}")
    }
  }

  def isUserAdmin(userId: Long) = userId == adminId

  val noAction = (_: Any) => ()

  def isAdmin(action: Action[Any])(noAdminAction: Action[Any] = noAction)(implicit msg: Message) = {
    if(isUserAdmin(msg.chat.id)){
      action()
    }else{
      noAdminAction()
    }
  }

  def isNotCommand(action: Action[Any])(implicit msg: Message) = {
    if(! msg.text.getOrElse("").startsWith("/")){
      action()
    }
  }

  def changeUserPermission(uid: Long, permission: BotUserPermission) = {
    val userH  = getUser(uid)
    userH.map{
      uH =>
        updateUser(uH.copy(user=uH.user.copy(permission = permission)))
     }
  }

  def userFromMessage(msg: Message) = {
    msg.from.get
  }

}
