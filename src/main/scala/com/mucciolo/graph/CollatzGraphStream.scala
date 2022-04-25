package com.mucciolo.graph

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl._

object CollatzGraphStream {

  private val CollatzGraph: Graph[FlowShape[Long, Long], NotUsed] = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val input: UniformFanInShape[Long, Long] = builder.add(Merge(3))
    val broadcast = builder.add(Broadcast[Long](2))
    val moduloTwo: UniformFanOutShape[Long, Long] = builder.add(Partition[Long](2, n => (n % 2).toInt))
    val even: Flow[Long, Long, NotUsed] = Flow.fromFunction[Long, Long](_ / 2)
    val odd: Flow[Long, Long, NotUsed] = Flow.fromFunction[Long, Long](3 * _ + 1)

    input ~> broadcast
             broadcast.out(0).filter(_ > 1) ~> moduloTwo
                                               moduloTwo.out(0) ~> even ~> input.in(0)
                                               moduloTwo.out(1) ~> odd  ~> input.in(1)

    FlowShape(input.in(2), broadcast.out(1))
  }

  def from(n: Long): Source[Long, NotUsed] = {
    Source.single(n).via(CollatzGraph).takeWhile(_ != 1, inclusive = true)
  }

}
