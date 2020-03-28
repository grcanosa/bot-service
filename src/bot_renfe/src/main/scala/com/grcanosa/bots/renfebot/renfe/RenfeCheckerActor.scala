package com.grcanosa.bots.renfebot.renfe

import akka.actor.{Actor, ActorContext, ActorRef}
import com.grcanosa.bots.renfebot.model.{Journey, JourneyCheck, Trip}
import com.grcanosa.bots.renfebot.renfe.RenfeCheckerActor.CheckJourney
import com.grcanosa.telegrambot.model.BotUser

object RenfeCheckerActor{

  case class CheckJourney(trip: Journey, users: Seq[ActorRef])

}

class RenfeCheckerActor(val driverUrl: String) extends Actor {

  implicit val ec = context.dispatcher

  val renfeChecker = new RenfeChecker(driverUrl)

  override def receive = {
    case CheckJourney(journey,users) => {
      val res = renfeChecker.checkJourney(journey)
      val journeyCheck = JourneyCheck(journey,res.trips)
      users.foreach{ botUser =>
        botUser ! journeyCheck
      }
    }
  }
}
