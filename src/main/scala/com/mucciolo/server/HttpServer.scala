package com.mucciolo.server

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives
import akka.util.ByteString
import com.mucciolo.actor.CollatzSequenceActor
import com.mucciolo.actor.CollatzSequenceActor.GetSequence
import com.mucciolo.stream.CollatzGraphStream

import java.util.UUID
import scala.concurrent.Future

object HttpServer extends Directives {

  def start(implicit actorSystem: ActorSystem[GetSequence]): Future[Http.ServerBinding] = {

    val route = {
      pathPrefix("collatz-stream") {
        concat(
          path("graph" / LongNumber) { initialNumber =>
            get {
              val data = CollatzGraphStream.from(initialNumber).map(n => ByteString(s"$n, "))
              complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, data))
            }
          },
          path("actor" / LongNumber) { initialNumber =>

            val requestId = UUID.randomUUID()
            val data = CollatzSequenceActor.stream(requestId, initialNumber).map(n => ByteString(s"$n, "))

            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, data))
          }
        )
      }
    }

    Http().newServerAt("localhost", 8080).bind(route)
  }
}
