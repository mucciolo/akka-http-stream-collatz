package com.mucciolo.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.util.ByteString
import com.mucciolo.stream.CollatzGraphStream

import scala.concurrent.{ExecutionContextExecutor, Future}

object HttpServer {

  def start(): Future[Http.ServerBinding] = {

    implicit val actorSystem: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "collatz-stream")
    implicit val executionContext: ExecutionContextExecutor = actorSystem.executionContext

    val route = {
      pathPrefix("collatz" / "stream") {
        concat(
          path("graph" / LongNumber) { seed =>
            get {
              complete(
                HttpEntity(
                  ContentTypes.`text/plain(UTF-8)`,
                  CollatzGraphStream.from(seed).map(n => ByteString(s"$n, "))))
            }
          }
        )
      }
    }

    Http().newServerAt("localhost", 8080).bind(route)
  }
}
