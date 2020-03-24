package com.grcanosa.bots.renfebot.model

case class Journey(origin: String,
                   destination: String,
                   departureDate: String,
                   returnDate: Option[String])
