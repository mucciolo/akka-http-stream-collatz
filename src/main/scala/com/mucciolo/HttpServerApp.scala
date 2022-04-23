package com.mucciolo

import akka.actor.typed.ActorSystem
import com.mucciolo.actor.CollatzSequenceActor
import com.mucciolo.actor.CollatzSequenceActor.GetSequence
import com.mucciolo.server.HttpServer

object HttpServerApp extends App {

  val actorSystem: ActorSystem[GetSequence] = ActorSystem(CollatzSequenceActor(), "collatz-stream")

  HttpServer.start(actorSystem)
}
