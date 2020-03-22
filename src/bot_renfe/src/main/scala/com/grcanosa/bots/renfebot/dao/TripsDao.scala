package com.grcanosa.bots.renfebot.dao

import com.grcanosa.bots.renfebot.dao.TripsDao.TripMongo
import com.grcanosa.bots.renfebot.model.Trip
import com.grcanosa.telegrambot.dao.mongo.MongoResultsMappings
import com.grcanosa.telegrambot.model.BotUser
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import scala.concurrent.ExecutionContext

object TripsDao {

  case class TripMongo(userId: Long,
                       active: Boolean,
                      trip: Trip)


  def toTripMongo(botUser: BotUser,trip:Trip) = {
    TripMongo(botUser.id,true,trip)
  }

  val trip2Tuple = (trip: Trip) => (trip.origin,trip.destination,trip.departureDate,trip.returnDate)

  type TripMapType = Map[(String,String,String,Option[String]), Set[Long]]

  val zeroMap: TripMapType = Map.empty


  val foldFunction: (TripMapType, TripMongo) => TripMapType =
    (map: TripMapType, tripMongo: TripMongo) => {
    map.get(trip2Tuple(tripMongo.trip)) match {
      case Some(setUserId) => map + (trip2Tuple(tripMongo.trip) -> (setUserId ++ Set(tripMongo.trip)))
      case None => map + (trip2Tuple(tripMongo.trip) -> Set(tripMongo.userId))
    }
  }


  def tripsMongo2UniqueTrips(trips: Seq[TripMongo]) = {
    trips.foldLeft(zeroMap)(foldFunction)
  }

}

class TripsDao(val host: String
               , val port: Int
               , val databaseName: String)
              (implicit val executionContext: ExecutionContext)
  extends MongoResultsMappings {

  import TripsDao._

  private lazy val mongo: MongoClient = MongoClient(s"mongodb://$host:$port")

  private lazy val codecRegistry: CodecRegistry = fromRegistries(
    fromProviders(classOf[TripMongo]),
    DEFAULT_CODEC_REGISTRY )

  private lazy val database: MongoDatabase = mongo.getDatabase(databaseName).withCodecRegistry(codecRegistry)

  private lazy val trips: MongoCollection[TripMongo] = database.getCollection("trips")

  def addTrip(user: BotUser,trip:Trip) = {
    trips.insertOne(toTripMongo(user,trip)).toBooleanFuture()
  }

  def getTripsForUser(user: BotUser) = {
    trips.find(equal("userId",user.id)).toFuture().map(_.map(_.trip))
  }

  def removeTrip(user: BotUser, trip: Trip) = {
    trips.findOneAndUpdate(and(
      equal("userId",user.id),
      equal("trip",trip)
      )
      ,
      set("active",false)
    ).toFuture()
  }

  def getDistinctActiveTrips() = {
    trips.find(equal("active",true))
      .toFuture()
      .map(tripsMongo2UniqueTrips)

  }

  def markOldTripsAsInactive(lastDate: String) = {
    trips.updateMany(and(
      equal("active",true),
      lt("trip.departureDate",lastDate)
    ),
      set("active",false)
    ).toFuture()
  }

}
