package com.mucciolo.actor

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.mucciolo.actor.CollatzSequenceActor.{GetSequence, CollatzSequence}
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor2}
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.UUID

class CollatzSequenceActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike with TableDrivenPropertyChecks {

  private val expectedSequences: TableFor2[Long, List[Long]] = Table(
    ("n", "seq"),
    (1, List(1)),
    (2, List(2, 1)),
    (3, List(3, 10, 5, 16, 8, 4, 2, 1)),
    (4, List(4, 2, 1)),
    (5, List(5, 16, 8, 4, 2, 1)),
    (6, List(6, 3, 10, 5, 16, 8, 4, 2, 1)),
    (7, List(7, 22, 11, 34, 17, 52, 26, 13, 40, 20, 10, 5, 16, 8, 4, 2, 1)),
    (8, List(8, 4, 2, 1)),
    (9, List(9, 28, 14, 7, 22, 11, 34, 17, 52, 26, 13, 40, 20, 10, 5, 16, 8, 4, 2, 1)),
    (10, List(10, 5, 16, 8, 4, 2, 1))
  )

  "CollatzSequenceActor" must {

    "return the correct sequence" in {
      forAll (expectedSequences) { (n, seq) =>
        assertSequence(n, seq)
      }
    }

  }

  private def assertSequence(initialNumber: Long, seq: List[Long]): CollatzSequence = {

    val probe = createTestProbe[CollatzSequence]()
    val computer = spawn(CollatzSequenceActor())
    val id = UUID.randomUUID().toString

    computer ! GetSequence(id, initialNumber, probe.ref)
    probe.expectMessage(CollatzSequence(id, seq))
  }
}
