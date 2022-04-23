package com.mucciolo

import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import com.mucciolo.actor.CollatzSequenceActor
import com.mucciolo.actor.CollatzSequenceActor.GetSequence
import com.mucciolo.server.HttpServer
import org.slf4j.{Logger, LoggerFactory}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object HttpServerApp extends App {

  private val log: Logger = LoggerFactory.getLogger(getClass)

  ConfigSource.default.load[HttpServer.Config] match {

    case Left(failures) =>
      log.error(failures.prettyPrint())

    case Right(config) =>
      implicit val actorSystem: ActorSystem[GetSequence] = ActorSystem(CollatzSequenceActor(), "collatz-stream")
      implicit val executionContext: ExecutionContextExecutor = actorSystem.executionContext

      HttpServer.run(config).onComplete {

        case Success(binding) =>
          log.info("Server started on http://{}:{}", binding.localAddress.getHostName, binding.localAddress.getPort)
          CoordinatedShutdown(actorSystem).addJvmShutdownHook(() => {
            binding.terminate(10.seconds)
          })

        case Failure(exception) =>
          log.error(exception.getMessage)
          actorSystem.terminate()
      }

  }

}
