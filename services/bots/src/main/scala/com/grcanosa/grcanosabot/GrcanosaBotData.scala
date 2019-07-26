package com.grcanosa.grcanosabot

object GrcanosaBotData {

  import com.grcanosa.telegrambot.utils.BotUtils._

  import com.grcanosa.grcanosabot.utils.GrcanosaBotUtils._

  val saraId = configGrcanosa.getLong("bot.saraId")


  val startText = {
    "Hola, soy el bot".emojize
  }

  val helpText = {
    """Esto es lo que puedo hacer:
      |/start - Imprime el mensaje de bienvenida
      |/help - Imprime esta ayuda.
      |/cancelconversation - Cancela la conversación actual
    """.stripMargin.emojize
  }

  val requestingPermissionText = "Pidiendo acceso a admin.".emojize

  val notAllowedText = "No tienes permiso para usar este bot.".emojize


  val cosasRealmenteBonitasSoloUnaPersonaText = "Lo siento, las cosas realmente bonitas solo se las digo a una persona"

  def noSeasPresumidoText(name: String) = Seq(
    s"$name, no seas tan presumido!!"
  ).chooseRandom().get
}
