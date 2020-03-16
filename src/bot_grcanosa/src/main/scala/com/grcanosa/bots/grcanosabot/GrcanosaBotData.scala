package com.grcanosa.bots.grcanosabot

object GrcanosaBotData {

  import com.grcanosa.telegrambot.utils.BotUtils._

  val saraId = configGrcanosa.getLong("bot.saraId")


  val startText = {
    "Hola, soy el bot de Gonzalo. Escribe /help para ver con que te puedo ayudar".emojize
  }

  val helpText = {
    """Esto es lo que puedo hacer:
      |/start - Imprime el mensaje de bienvenida
      |/help - Imprime esta ayuda.
      |/dimealgobonito
      |/dimealgorealmentebonito
      |/termo15 - Añade 15 minutos a la temporizacion del termo.
    """.stripMargin.emojize
  }

  val requestingPermissionText = "Pidiendo acceso a admin.".emojize

  val notAllowedText = "No tienes permiso para usar este bot.".emojize


  val cosasRealmenteBonitasSoloUnaPersonaText = "Lo siento, las cosas realmente bonitas solo se las digo a una persona"

  def noSeasPresumidoText(name: String) = Seq(
    s"$name, no seas tan presumido!!"
  ).chooseRandom().get
}
