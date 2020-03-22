package com.grcanosa.bots.renfebot.renfe

import akka.actor.Actor
import com.grcanosa.bots.renfebot.model.Trip
import com.grcanosa.bots.renfebot.renfe.RenfeCheckerActor.CheckTrip

object RenfeCheckerActor{

  case class CheckTrip(trip: Trip, users: Set[Long])

}

class RenfeCheckerActor extends Actor {

  override def receive = {
    case CheckTrip(trip,users) => {

    }

  }

}
