package com.mucciolo.actor

import akka.stream.testkit.scaladsl.TestSink
import com.mucciolo.stream.CollatzGraphStream.from

final class CollatzGraphStreamSpec extends CollatzSequenceTest {

  "CollatzGraphStream" must {

    "return the correct sequence" in {
      forAll (expectedSequences) { (n, seq) =>
        from(n).runWith(TestSink[Long]()).request(seq.length).expectNextN(seq).expectComplete()
      }
    }

  }

}
