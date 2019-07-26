package com.grcanosa.lovebot.model

import com.grcanosa.lovebot.model.Relationship.RelationshipType


object Relationship{
  sealed abstract class RelationshipType(val value: Short)

  case object RELATIONSHIP_NONE extends RelationshipType(0)
  case object RELATIONSHIP_COUPLE extends RelationshipType(1)
  case object RELATIONSHIP_MARRIED extends RelationshipType(2)
  case object RELATIONSHIP_AFFAIR extends RelationshipType(3)



}



case class Relationship(person1: Person, person2: Person, relationship: RelationshipType)
