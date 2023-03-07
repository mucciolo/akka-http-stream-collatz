package com.mucciolo.actor

import akka.stream.testkit.scaladsl.TestSink
import com.mucciolo.graph.CollatzGraphStream

final class CollatzGraphStreamSpec extends CollatzSequenceTest {

  "CollatzGraphStream" when {
    "from(n)" should {
      "return the Collatz sequence starting with n" in {
        forAll(expectedSequences) { (n, seq) =>
          CollatzGraphStream.from(n)
            .runWith(TestSink[Long]())
            .request(seq.length)
            .expectNextN(seq)
            .expectComplete()
        }
      }
    }
  }

}
