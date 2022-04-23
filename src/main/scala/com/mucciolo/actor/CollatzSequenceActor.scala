package com.mucciolo.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.mucciolo.actor.CollatzSequenceActor.SequenceElement
import com.mucciolo.actor.Mapper.Apply

object CollatzSequenceActor {

  sealed trait Message
  final case class GetSequence(id: String, initialNumber: Long, replyTo: ActorRef[CollatzSequence]) extends Message
  final case class SequenceElement(id: String, value: Long) extends Message
  final case class CollatzSequence(id: String, seq: List[Long])

  private var idToActorRef = Map.empty[String, ActorRef[CollatzSequence]]
  private var idToSequence = Map.empty[String, List[Long]]

  def apply(): Behavior[Message] =
    Behaviors.setup { context =>

      val evenMapper = context.spawn(EvenMapper(), "even-mapper")
      val oddMapper = context.spawn(OddMapper(), "odd-mapper")

      Behaviors.logMessages(
        Behaviors.receiveMessage { message =>
          message match {

            case GetSequence(id, initialNumber, replyTo) =>

              if (initialNumber == 1) {
                replyTo ! CollatzSequence(id, List(initialNumber))
              } else {
                idToActorRef += id -> replyTo
                idToSequence += id -> List(initialNumber)
                val mapper = if (initialNumber % 2 == 0) evenMapper else oddMapper
                mapper ! Mapper.Apply(id, initialNumber, context.self)
              }

            case SequenceElement(id, value) =>

              if (value == 1) {
                val replyTo = idToActorRef(id)
                val seq = (value :: idToSequence(id)).reverse
                replyTo ! CollatzSequence(id, seq)
                idToActorRef -= id
              } else {
                idToSequence += id -> (value :: idToSequence(id))
                val mapper = if (value % 2 == 0) evenMapper else oddMapper
                mapper ! Mapper.Apply(id, value, context.self)
              }
          }

          Behaviors.same
        }
      )
    }
}

object Mapper {
  final case class Apply(id: String, n: Long, replyTo: ActorRef[SequenceElement])
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