package com.grcanosa.bots.renfebot.model

case class Trip(origin: String,
                destination: String,
                departureDate: String,
                returnDate: Option[String])
