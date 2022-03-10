package com.adalbert.benchmarks

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import scala.collection.mutable

@State(Scope.Benchmark)
class SBenchmark {

  @Benchmark
  @Fork(1)
  @Measurement(iterations = 1)
  @Warmup(iterations = 1)
  def testMethodScala(bh: Blackhole): Unit = {
    val collection: mutable.HashSet[Integer] = new mutable.HashSet[Integer]
    for (i <- 0 until 1000) collection.add(i)
    for (i <- 0 until 1000) {
      val value: Int = if (i % 4 == 0) -i else i
      i % 5 match {
        case 0 => collection.add(value)
        case 1 => collection.remove(value)
        case 2 => bh.consume(collection.contains(value))
        case 3 => collection.addAll(Vector(value - 1, value, value + 1))
        case 4 => collection.subtractAll(Vector(value - 1, value, value + 1))
      }
    }
    collection.clear()
  }

}