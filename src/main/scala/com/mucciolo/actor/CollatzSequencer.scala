package com.mucciolo.actor

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.stream.scaladsl.Source
import akka.stream.typed.scaladsl.ActorSource
import akka.stream.{CompletionStrategy, OverflowStrategy}
import com.mucciolo.actor.CollatzSequencer.ElementComputed
import com.mucciolo.actor.CollatzMapper.NextElement

import java.util.UUID

object CollatzSequencer {

  sealed trait Response
  final case class SequenceElement(n: Long) extends Response
  final object SequenceComplete extends Response

  sealed trait Message

  sealed trait Query extends Message
  final case class GetSequence(initialNumber: Long, replyTo: ActorRef[Response]) extends Query

  sealed trait Event extends Message
  final case class ElementComputed(sequenceId: UUID, n: Long) extends Event

  private var requesterById = Map.empty[UUID, ActorRef[Response]]

  def apply(): Behavior[Message] =
    Behaviors.setup { context =>

      val evenMapper = context.spawn(EvenCollatzMapper(), "even-mapper")
      val oddMapper = context.spawn(OddCollatzMapper(), "odd-mapper")

      Behaviors.receiveMessage { message =>
        message match {

          case GetSequence(initialNumber, replyTo) =>

            replyTo ! SequenceElement(initialNumber)

            if (initialNumber == 1) {
              replyTo ! SequenceComplete
            } else {
              val sequenceId = UUID.randomUUID()
              requesterById += sequenceId -> replyTo
              val mapper = if (initialNumber % 2 == 0) evenMapper else oddMapper
              mapper ! CollatzMapper.NextElement(sequenceId, initialNumber, context.self)
            }

          case ElementComputed(sequenceId, n) =>

            val replyTo = requesterById(sequenceId)
            replyTo ! SequenceElement(n)

            if (n == 1) {
              replyTo ! SequenceComplete
              requesterById -= sequenceId
            } else {
              val mapper = if (n % 2 == 0) evenMapper else oddMapper
              mapper ! CollatzMapper.NextElement(sequenceId, n, context.self)
            }
        }

        Behaviors.same
      }
    }

  def stream(initialNumber: Long)
            (implicit collatzSequenceActor: ActorSystem[GetSequence]): Source[Long, NotUsed] = {

    val (streamActor, stream) = ActorSource.actorRef[Response](
      completionMatcher = {
        case SequenceComplete => CompletionStrategy.draining
      },
      failureMatcher = PartialFunction.empty,
      bufferSize = 1024,
      overflowStrategy = OverflowStrategy.fail
    ).preMaterialize()

    collatzSequenceActor ! GetSequence(initialNumber, streamActor)

    stream.collect { case SequenceElement(n) => n }
  }
}

object CollatzMapper {
  final case class NextElement(id: UUID, n: Long, replyTo: ActorRef[ElementComputed])
}

trait CollatzMapper {

  def map(f: Long => Long): Behavior[NextElement] = Behaviors.receiveMessage { message =>
    val nextElement = f(message.n)
    message.replyTo ! ElementComputed(message.id, nextElement)
    Behaviors.same
  }

}

object EvenCollatzMapper extends CollatzMapper {
  def apply(): Behavior[NextElement] = map(_ / 2)
}

object OddCollatzMapper extends CollatzMapper {
  def apply(): Behavior[NextElement] = map(3 * _ + 1)
}