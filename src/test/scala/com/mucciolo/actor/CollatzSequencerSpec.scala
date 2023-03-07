package com.mucciolo.actor

import com.mucciolo.actor.CollatzSequencer._

final class CollatzSequencerSpec extends CollatzSequenceTest {

  "CollatzSequencer" when {
    "told GetSequence(n, ref)" should {
      "return the Collatz sequence starting with n" in {
        forAll(expectedSequences) { (n, seq) =>
          val probe = createTestProbe[Response]()
          val computer = spawn(CollatzSequencer())

          computer ! GetSequence(n, probe.ref)
          seq.foreach(n => probe.expectMessage(SequenceElement(n)))
          probe.expectMessage(SequenceComplete)
        }
      }
    }
  }

}
