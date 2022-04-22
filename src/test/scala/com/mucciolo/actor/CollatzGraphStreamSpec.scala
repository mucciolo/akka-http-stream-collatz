package com.mucciolo.actor

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.stream.testkit.scaladsl.TestSink
import com.mucciolo.stream.CollatzGraphStream.from
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor2}
import org.scalatest.wordspec.AnyWordSpecLike

class CollatzGraphStreamSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike with TableDrivenPropertyChecks {

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

  "CollatzGraphStream" must {

    "return the correct sequence" in {
      forAll (expectedSequences) { (n, seq) =>
        from(n).runWith(TestSink[Long]()).request(seq.length).expectNextN(seq).expectComplete()
      }
    }

  }

}
