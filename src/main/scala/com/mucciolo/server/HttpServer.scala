package com.mucciolo.server

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import com.mucciolo.actor.CollatzSequencer
import com.mucciolo.actor.CollatzSequencer.GetSequence
import com.mucciolo.graph.CollatzGraphStream
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.Future

object HttpServer extends Directives with SprayJsonSupport with DefaultJsonProtocol {

  final case class Config(host: String, port: Int)

  private final case class CollatzSequenceElement(index: Long, value: Long)

  private implicit val sequenceElementFormat: RootJsonFormat[CollatzSequenceElement] =
    jsonFormat2(CollatzSequenceElement.apply)
  private val newLine = ByteString("\n")
  private implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport.json()
      .withFramingRenderer(Flow[ByteString].map(_ ++ newLine))

  def run(config: Config)(implicit actorSystem: ActorSystem[GetSequence]): Future[Http.ServerBinding] = {
    Http().newServerAt(config.host, config.port).bind(buildRoute)
  }

  private def buildRoute(implicit actorSystem: ActorSystem[GetSequence]) = {

    pathPrefix("collatz-stream") {
      concat(
        path("graph" / LongNumber) { initialNumber =>
          get {
            complete(indexedStream(CollatzGraphStream.from(initialNumber)))
          }
        },
        path("actor" / LongNumber) { initialNumber =>
          get {
            complete(indexedStream(CollatzSequencer.stream(initialNumber)))
          }
        }
      )
    }

  }

  private def indexedStream(stream: Source[Long, NotUsed]): Source[CollatzSequenceElement, NotUsed] = {
    stream.zipWithIndex.map {
      case (value, index) => CollatzSequenceElement(index, value)
    }
  }
}
