package com.grcanosa.testapp

import com.grcanosa.telegrambot.dao.redis.BotUserRedisDao

object TestApp1 extends App {

 implicit val system = akka.actor.ActorSystem("test")

  val redis = new BotUserRedisDao("localhost"
    ,6379
    ,"testkeybase")



}
