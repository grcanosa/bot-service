package com.grcanosa.bots.renfebot.model

case class Trip(departureTimestamp: Long,
                durationMinutes: Long,
                departureHour: String,
                arrivalHour: String,
                disponible: Boolean,
                price: Option[Float],
                tipo: Option[String],
                clase: Option[String],
                tarifa: Option[String])
