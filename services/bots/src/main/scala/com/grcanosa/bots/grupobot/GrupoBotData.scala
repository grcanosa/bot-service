package com.grcanosa.bots.grupobot

import com.grcanosa.bots.grupobot.GrupoBotHugChain.HugChain
import com.grcanosa.bots.grupobot.utils.GrupoUtils
import com.vdurmont.emoji.EmojiParser

import scala.concurrent.duration._
import scala.util.Try

object GrupoBotData {

  import com.grcanosa.telegrambot.utils.BotUtils._
  import GrupoUtils._

  implicit class BotStrings(s: String){
    def bottext: String = EmojiParser.parseToUnicode(":robot_face::speech_balloon: "+s)
  }


  val conversationDuration = Try{configGrupo.getInt("grupobot.conversation.minutes")}.getOrElse(15) minutes

  BOTLOG.info(s"Conversation set to last $conversationDuration")

  val noConversationReadyText = (name: String) => {
    s"Ahora mismo no te puedo conectar con nadie $name, inténtalo de nuevo más tarde!".bottext
  }

  val noConversationAssigned = (name: String) => {
    s"$name, no conectad@ ahora mismo.".bottext
  }

  val newConversationText = (name: String) => {
    s"Conexión establecida.".bottext
  }

  val conversationEndedText = {
    "Conexión finalizada.".bottext
  }

  val startText = {
    List(
      "Hola, soy el bot de Los del Olmo :deciduous_tree:. Mi propósito es conectarte aleatoriamente con otros miembros del grupo."
      ,s"""Cada vez que escribas al bot enviaré tus mensajes aleatoriamente a otro miembro del grupo.
         | Ninguno de los dos sabrá quién es el otro (excepto que os lo digáis, claro).
         | Si ella/él te contesta, sus mensajes te llegarán a ti. Diez minutos después de que os intercambiéis el
         | último mensaje la conexión terminará y podréis empezar una nueva.
         |También podréis cancelar la conexión actual escribiendo: /cancelconexion.
         |""".stripMargin.replaceAll("\n","").emojize
      ,
      s"""Casi todos los mensajes que recibirás aquí serán escritos por alguien del grupo.
         | Los mensajes que te envíe yo (el bot) siempre empezarán por :robot_face::speech_balloon:
         | para que puedas indentificarlos facilmente.
         |""".stripMargin.replaceAll("\n","").emojize
    ).mkString("\n").bottext
  }

  val helpText = {
    """Esto es lo que puedo hacer:
      |/start - Imprime el mensaje de bienvenida
      |/help - Imprime esta ayuda.
      |/cancelconexion - Cancela la conexión actual
    """.stripMargin.bottext
  }

  val requestingPermissionText = "Pidiendo acceso a admin.".bottext

  val notAllowedText = "No tienes permiso para usar este bot.".bottext

  val permissionGrantedText = s"Te han dado permiso para usarme. Pulsa en /start para empezar".bottext

  val hugChainSeparator = ";"
  val hugChainCallbackDataKeyword = "HUGCHAIN"

  def hugChainCallbackData(id:Long, chainId: Long) = Seq(hugChainCallbackDataKeyword,chainId.toString,id.toString).mkString(hugChainSeparator)

  def getCallbackData(data: Option[String]) = data
            .map(_.split(hugChainSeparator).toSeq match
                   {
                    case Seq(_,chainId,id) => (chainId.toLong,id.toLong)
                  })


  def abrazarRandom = Seq("abrazar","achuchar","estrujar").chooseRandom().get

  def abrazadoRandom = Seq("abrazado", "achuchado","estrujado").chooseRandom().get

  def abrazosRandom = Seq("abrazos","achuchones", "estrujones").chooseRandom().get

  val hugChainStartedText = (name: String) => s"""$name, acabas de iniciar una cadena de ${abrazosRandom}! :smile: ¡Elige a quién quieres ${abrazarRandom}!""".stripMargin.bottext

  val hugChainEndText = (chain: HugChain) =>
    s"""${chain.users.head.user.name}, eres el final de una cadena de $abrazosRandom. :smile::smiley::confetti_ball::tada: Acabas de ser $abrazadoRandom
       | por ${chain.users.tail.view.init.map(_.user.name).mkString(",")} y ${chain.users.tail.last.user.name}.
       | Puedes empezar otra cadena de abrazos escribiendo /cadenadeabrazos.
       |""".bottext.stripMargin


  val hugChainContinueText = (chain: HugChain) =>
    s"""${chain.users.head.user.name}, ${chain.users.tail.head.user.name} acaba de incluirte en
       | una cadena de ${abrazosRandom} virtual. :smile::smiley::open_mouth::smile::smiley: ¡Puedes elegir a otra persona para continuar la cadena!
       |""".bottext.stripMargin

  val chainContinuingText = (chain: HugChain) =>
    s"¡¡¡Bieeeeeennnn!!!, la cadena continúa hacia ${chain.users.head.user.name}".bottext
}
