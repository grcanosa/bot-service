// package com.grcanosa.bots.renfebot

// import akka.actor.typed._
// import akka.actor.typed.scaladsl.Behaviors

// import akka.actor.typed.scaladsl.AskPattern._
// import akka.actor.typed.scaladsl.LoggerOps
// import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
// import com.grcanosa.bots.renfebot.RenfeChecker.RenfeCheckerMessage
// import com.grcanosa.bots.renfebot.model.Trip

// object RenfeChecker {

//   sealed abstract trait RenfeCheckerMessage

//   case class CheckTrip(trip: Trip) extends RenfeCheckerMessage


//   def behaviour : Behavior[RenfeCheckerMessage]= Behaviors.receive{ (ctx, msg) => msg match {
//     case CheckTrip(trip) => {

//     }
//     }
//     Behaviors.same
//   }


// }
