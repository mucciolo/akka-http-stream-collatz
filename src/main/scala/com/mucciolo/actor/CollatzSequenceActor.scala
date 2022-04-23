package com.mucciolo.actor

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.stream.scaladsl.Source
import akka.stream.{CompletionStrategy, OverflowStrategy}
import akka.stream.typed.scaladsl.ActorSource
import com.mucciolo.actor.CollatzSequenceActor.SequenceElement
import com.mucciolo.actor.Mapper.Apply

import java.util.UUID

object CollatzSequenceActor {

  sealed trait Event
  sealed trait RequestEvent extends Event
  sealed trait SequenceEvent extends Event
  final case class GetSequence(id: UUID, initialNumber: Long, replyTo: ActorRef[SequenceEvent]) extends RequestEvent
  final case class SequenceElement(id: UUID, n: Long) extends SequenceEvent
  final case class SequenceComplete(id: UUID) extends SequenceEvent

  private var idToActorRef = Map.empty[UUID, ActorRef[SequenceEvent]]

  def apply(): Behavior[Event] =
    Behaviors.setup { context =>

      val evenMapper = context.spawn(EvenMapper(), "even-mapper")
      val oddMapper = context.spawn(OddMapper(), "odd-mapper")

      Behaviors.logMessages(
        Behaviors.receiveMessage { message =>
          message match {

            case GetSequence(id, initialNumber, replyTo) =>

              replyTo ! SequenceElement(id, initialNumber)

              if (initialNumber == 1) {
                replyTo ! SequenceComplete(id)
              } else {
                idToActorRef += id -> replyTo
                val mapper = if (initialNumber % 2 == 0) evenMapper else oddMapper
                mapper ! Mapper.Apply(id, initialNumber, context.self)
              }

            case element @ SequenceElement(id, n) =>

              val replyTo = idToActorRef(id)
              replyTo ! element

              if (n == 1) {
                replyTo ! SequenceComplete(id)
                idToActorRef -= id
              } else {
                val mapper = if (n % 2 == 0) evenMapper else oddMapper
                mapper ! Mapper.Apply(id, n, context.self)
              }
          }

          Behaviors.same
        }
      )
    }

  def stream(requestId: UUID, initialNumber: Long)
            (implicit collatzSequenceActor: ActorSystem[GetSequence]): Source[Long, NotUsed] = {

    val (streamActor, stream) = ActorSource.actorRef[SequenceEvent](
      completionMatcher = {
        case SequenceComplete(id) if id == requestId => CompletionStrategy.draining
      },
      failureMatcher = PartialFunction.empty,
      bufferSize = 2048,
      overflowStrategy = OverflowStrategy.fail
    ).preMaterialize()

    collatzSequenceActor ! GetSequence(requestId, initialNumber, streamActor)

    stream.collect { case SequenceElement(id, n) if id == requestId => n }
  }
}

object Mapper {
  final case class Apply(id: UUID, n: Long, replyTo: ActorRef[SequenceElement])
}

trait Mapper {

  def behaviour(f: Long => Long): Behavior[Apply] = Behaviors.receiveMessage { message =>
    message.replyTo ! SequenceElement(message.id, f(message.n))
    Behaviors.same
  }

}

object EvenMapper extends Mapper {
  def apply(): Behavior[Apply] = behaviour(_ / 2)
}

object OddMapper extends Mapper {
  def apply(): Behavior[Apply] = behaviour(3 * _ + 1)
}