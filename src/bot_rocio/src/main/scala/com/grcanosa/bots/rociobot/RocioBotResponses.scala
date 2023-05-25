package com.grcanosa.bots.rociobot

import com.bot4s.telegram.models.{KeyboardButton, ReplyKeyboardMarkup, ReplyKeyboardRemove}
import com.vdurmont.emoji.EmojiParser

trait RocioBotResponses {

  implicit class RandomFromList(li: Seq[Any]) {

    import scala.util.Random

    val random = new Random

    def chooseRandom(): Option[Any] = li.length match {
      case 0 => None
      case _ => Some(li(random.nextInt(li.length)))
    }
  }

  implicit class RandomFromStringList(li: Seq[String]) {
    import scala.util.Random
    val random = new Random
    def chooseRandomStr(): String = li.chooseRandom().getOrElse("").toString
  }

  implicit class EmojiString(s: String) {
    def emojize: String = EmojiParser.parseToUnicode(s)
  }

  def startupText: String =
    """
      |¡¡¡Bienvenido/a!!!
      |
      |Aprovechando que Rocío nos ha juntado a todos hemos preparado un juego para ver cuánto la conoces en realidad.
      |
      |Presentamos...
      |
      |¡¡¡50x15 RR (Rocío Regueiro) Edition!!!
      |
      |Te iré mandando preguntas con 4 opciones y tendrás que contestarme con A,B,C o D con la respuesta elegida.
      |Se darán los siguientes puntos por pregunta:
      |Acierto a la primera => 10 puntos.
      |Acierto a la segunda =>  5 puntos.
      |Acierto a la tercera =>  3 puntos.
      |Acierto a la cuarta =>  1 punto.
      |
      | Los 5 primeros que vayan a la organizadora con una puntuación de 100 puntos tendrán un pequeño regalo de conmemoración!
      |""".stripMargin.emojize


  def getUnknownResponse: String = {
    Seq(
      "No entiendo que me quieres decir... :cry:",
      "Ahí me has pillado... xD :flushed:",
      "No se de qué me hablas...",
      "No te entiendo, lo siento..."
    ).chooseRandomStr().emojize
  }

  def notAnswerResponse: String = {
    Seq(
      "No entiendo que me quieres decir... :cry:, contesta con A,B,C,D",
      "Necesito que me contestes con A,B,C,D :flushed:",
      "¿¿¿Yo para que te he dado opciones???"
    ).chooseRandomStr().emojize
  }

  def goodAnswer(points: Int): String = Seq(
    "¡Acertaste!"
    , s"¡Genial!"
    , s"Estupendo, ya llevas $points puntos"
  ).chooseRandomStr().emojize

  def badAnswer: String = Seq(
    "!Intentalo de nuevo!"
    , "Esa no era la respuesta que buscaba... "
    , "Uishhh, casi. A la próxima seguro que lo consigues."
  ).chooseRandomStr().emojize


  val answersKeyboard = ReplyKeyboardMarkup(Seq(
    Seq(KeyboardButton("A"), KeyboardButton("B"))
    , Seq(KeyboardButton("C"), KeyboardButton("D")))
    , oneTimeKeyboard = Some(true), resizeKeyboard = Some(true))

  //val removeKeyboard = ReplyKeyboardRemove(true)


  val dummyQuestions = List(
    Question("Question 1",List(10,5,3,1),"respA","respB","respC","respD","A")
    ,Question("Question 2",List(10,5,3,1),"respA","respB","respC","respD","B")
    ,Question("Question 3",List(10,5,3,1),"respA","respB","respC","respD","C")
  )

  val defaultPoints = List(10,5,3,1)

  val realQuestions = List(
     Question(
      "¿Cómo se llamaba el primer novio de Rocío ?"   , defaultPoints
       , "Rafa.", "Javi.", "Sergio.", "Eustaquio.", "A" )

    , Question(
      "¿Qué tocaba Rocío de pequeña para dormir?", defaultPoints
      , "Un peluche en forma de estrella.", "El pelo de su hermana.", "La tela del camisón de su madre."
      , "Una mantita de gasa.", "C"  )
//    , Question(
//      "¿Y cuándo ya era un poco más mayor?", defaultPoints
//      , "???", "La ballena de peluche de Begoña que tenía la misma textura"
//      , "???", "???", "B" )
    , Question(
      "¿Cómo llamaban a Rocío de pequeña?", defaultPoints
      , "Roci.", "A gritos."
      , "Ro.", "Peque.", "D" )
    , Question(
      "¿Qué frase le dice Rocío a todos sus pacientes?", defaultPoints
      , "Todo es cuestión de perspectiva."
      , "Aquí se viene llorado de casa."
      , "Espera aquí que entramos en un momentito."
      , "Soy lo más cuidando a los demás.", "C"
    )
    , Question(
      "¿Quién pilló a Rocío dándose el lote con Rafa cuando era una \"teenager\"?", defaultPoints
      , "Don Paquito, el cura.", "Su hermana mayor."
      , "Doña Marta, la panadera.", "Don Javier, el guardia civil.", "A"  )
    , Question(
      "¿Cuales son las fotos prohibidas de la infancia de Rocío?", defaultPoints
      , "Las de después de cortarse ella misma el pelo con 5 años."
      , "Las de la comunión."
      , "Las de su 8º cumpleaños."
      , "Las del año en el que se rompió los dientes.", "B" )
    , Question(
      "¿A dónde  llevó Rocío de excursión a toda su familia cuando eran pequeños?", defaultPoints
      , "Al Zoo", "Al parque de atracciones"
      , "A la catedral de la Almudena", "A una heladeria", "C")
    , Question(
      "¿Con quién jugaban Chus y Rocío al escondite en casa de los abuelos?", defaultPoints
      , "Con Begoña."
      , "Con Cristina y la tía Juana"
      , "Con sus primos."
      ,"Con nadie, a Rocio no le gustaba el escondite.", "B" )
    , Question(
      "¿Qué hace Rocío cuando le gusta mucho una cosa?", defaultPoints
      , "La usa hasta que se rompe/estropea."
      , "No usarla hasta que se puede comprar otra igual."
      , "Recomendarlo a todos sus amigos."
      , "Regalárselo a su familia.", "B"
    )
    , Question(
      "¿Con qué canción despertaba a toda la familia el padre de Rocio el 1 de enero todos los años?", defaultPoints
      , "Un año más de Mecano."
      , "Happy New Year de Abba."
      , "La marcha Radetzky."
      , "Thunderstruck de AC/DC", "C" )
    , Question(
      "¿Cómo llamaban Chus, Rocío y Begoña a la habitación en la que dormían?", defaultPoints
      , "La cueva.", "La aldea.", "El pueblo.", "La casita.", "B"
    )
    , Question(
      "¿Que plato es la especialidad de Rocío en la cocina?", defaultPoints
      , "Solomillo Wellington.", "Cocochas de bacalao en salsa verde."
      , "Ensalada con palitos de cangrejo.", "Tarta de zanahoria.", "C"
    )
    , Question(
      "¿Qué película ponían los padres de Rocio en el Súper 8 en su habitación?", defaultPoints
      , "El corto del Danubio Azul de Disney."
      , "Pesadilla en Elm Street."
      , "Bambi."
      , "Los goonies","A"
    )
    , Question(
      "¿Qué dos cosas no come nunca Rocío?", defaultPoints
      , "Cilantro y regalíz.", "Ajo y cebolla.", "Brocoli y guisantes.", "Té y chocolate.", "B"
    )
    , Question(
      "¿Qué frase pondría Rocío en su carpeta de instituto si tuviera 30 años menos?", defaultPoints
      , "Ama, ama y ensancha el alma."
      , "Autoexigencia cero, autoindulgencia cero."
      , "Lo que pasa en el grupo se queda en el grupo, o hay que restituirlo."
      , "Todas las respuestas anteriores son correctas.", "D"
    )

 )

  def finalPointsResponse(points: Int): String = {
    s"Estupendo, tu puntuación final es de $points puntos!!".emojize
  }

}
