package com.mucciolo.actor

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.mucciolo.actor.CollatzSequenceActor.{GetSequence, SequenceComplete, SequenceElement, SequenceEvent}
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor2}
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.UUID

class CollatzSequenceActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike with TableDrivenPropertyChecks {

  private val expectedSequences: TableFor2[Long, Seq[Long]] = Table(
    ("n", "seq"),
    (1, Seq(1)),
    (2, Seq(2, 1)),
    (3, Seq(3, 10, 5, 16, 8, 4, 2, 1)),
    (4, Seq(4, 2, 1)),
    (5, Seq(5, 16, 8, 4, 2, 1)),
    (6, Seq(6, 3, 10, 5, 16, 8, 4, 2, 1)),
    (7, Seq(7, 22, 11, 34, 17, 52, 26, 13, 40, 20, 10, 5, 16, 8, 4, 2, 1)),
    (8, Seq(8, 4, 2, 1)),
    (9, Seq(9, 28, 14, 7, 22, 11, 34, 17, 52, 26, 13, 40, 20, 10, 5, 16, 8, 4, 2, 1)),
    (10, Seq(10, 5, 16, 8, 4, 2, 1))
  )

  "CollatzSequenceActor" must {

    "return the correct sequence" in {
      forAll (expectedSequences) { (n, seq) =>
        assertSequence(n, seq)
      }
    }

  }

  private def assertSequence(initialNumber: Long, seq: Seq[Long]): SequenceComplete = {

    val probe = createTestProbe[SequenceEvent]()
    val computer = spawn(CollatzSequenceActor())
    val id = UUID.randomUUID()

    computer ! GetSequence(id, initialNumber, probe.ref)
    seq.foreach(n => probe.expectMessage(SequenceElement(id, n)))
    probe.expectMessage(SequenceComplete(id))
  }
}
