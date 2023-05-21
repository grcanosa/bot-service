package com.grcanosa.bots.rociobot


case class Question(question:String,
                    points: List[Int],
                    respA: String,
                    respB: String,
                    respC: String,
                    respD: String,
                    solution: String,
                    photo: Option[String] = None){

  def questionMsg(idx: Int) =
    s"""
       |Pregunta $idx:
       |${question}
       |A) ${respA}
       |B) ${respB}
       |C) ${respC}
       |D) ${respD}
    """.stripMargin

}