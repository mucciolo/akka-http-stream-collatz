package com.mucciolo.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives
import akka.util.{ByteString, Timeout}
import com.mucciolo.actor.CollatzSequenceActor
import com.mucciolo.actor.CollatzSequenceActor.{CollatzSequence, GetSequence}
import com.mucciolo.stream.CollatzGraphStream
import spray.json.DefaultJsonProtocol

import java.util.UUID
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContextExecutor, Future}

object HttpServer extends Directives with SprayJsonSupport with DefaultJsonProtocol {

  def start(implicit actorSystem: ActorSystem[CollatzSequenceActor.Message]): Future[Http.ServerBinding] = {

    implicit val executionContext: ExecutionContextExecutor = actorSystem.executionContext
    val collatzSequenceActor = actorSystem

    val route = {
      pathPrefix("collatz") {
        concat(
          path("stream" / LongNumber) { initialNumber =>
            get {
              complete(
                HttpEntity(
                  ContentTypes.`text/plain(UTF-8)`,
                  CollatzGraphStream.from(initialNumber).map(n => ByteString(s"$n, "))))
            }
          },
          path("actor" / LongNumber) { initialNumber =>
            get {
              implicit val timeout: Timeout = 2.seconds

              complete(
                (collatzSequenceActor ? (replyTo => GetSequence(UUID.randomUUID().toString, initialNumber, replyTo)))
                  .mapTo[CollatzSequence]
                  .map(_.seq)
              )

            }
          }
        )
      }
    }

    Http().newServerAt("localhost", 8080).bind(route)
  }
}
