package com.mucciolo.server

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import com.mucciolo.actor.CollatzSequenceActor
import com.mucciolo.actor.CollatzSequenceActor.GetSequence
import com.mucciolo.stream.CollatzGraphStream
import spray.json.DefaultJsonProtocol

import java.util.UUID
import scala.concurrent.Future

object HttpServer extends Directives with SprayJsonSupport with DefaultJsonProtocol {

  final case class Config(host: String, port: Int)

  final case class CollatzSequenceElement(index: Long, value: Long)

  private implicit val sequenceElementFormat = jsonFormat2(CollatzSequenceElement.apply)
  private val newLine = ByteString("\n")
  private implicit val jsonStreamingSupport = EntityStreamingSupport.json()
    .withFramingRenderer(Flow[ByteString].map(bs => bs ++ newLine))

  def run(config: Config)(implicit actorSystem: ActorSystem[GetSequence]): Future[Http.ServerBinding] = {
    Http().newServerAt(config.host, config.port).bind(buildRoute)
  }

  private def buildRoute(implicit actorSystem: ActorSystem[GetSequence]) = {

    pathPrefix("collatz-stream") {
      concat(
        path("graph" / LongNumber) { initialNumber =>
          get {
            complete(mapStreamToCollatzSequenceElement(CollatzGraphStream.from(initialNumber)))
          }
        },
        path("actor" / LongNumber) { initialNumber =>
          val requestId = UUID.randomUUID()
          complete(mapStreamToCollatzSequenceElement(CollatzSequenceActor.stream(requestId, initialNumber)))
        }
      )
    }

  }
  private def mapStreamToCollatzSequenceElement(stream: Source[Long, NotUsed]) = {
    stream.zipWithIndex.map {
      case (value, index) => CollatzSequenceElement(index, value)
    }
  }
}
