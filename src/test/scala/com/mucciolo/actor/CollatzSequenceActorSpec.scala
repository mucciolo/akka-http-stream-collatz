package com.mucciolo.actor

import com.mucciolo.actor.CollatzSequenceActor.{GetSequence, SequenceComplete, SequenceElement, SequenceEvent}

import java.util.UUID

final class CollatzSequenceActorSpec extends CollatzSequenceTest {

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
