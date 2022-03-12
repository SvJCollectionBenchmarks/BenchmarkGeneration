package com.adalbert.benchmarks

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import java.util
import java.util.HashSet
import scala.collection.mutable

@State(Scope.Benchmark)
class SBenchmark {

  private var firstSet: mutable.HashSet[Integer] = null
  private var secondSet: mutable.HashSet[Integer] = null
  private var thirdSet: mutable.HashSet[Integer] = null
  @Setup def prepareSets(): Unit = {
    firstSet = new mutable.HashSet[Integer]
    secondSet = new mutable.HashSet[Integer]
    thirdSet = new mutable.HashSet[Integer]
    for (i <- 0 until 10000) {
      if (i % 3 == 0) firstSet.add(i)
      if (i % 5 == 0) secondSet.add(i)
      if (i % 7 == 0) thirdSet.add(i)
    }
  }

  @Benchmark
  @Fork(1)
  @Measurement(iterations = 1)
  @Warmup(iterations = 1)
  def testMethodScala(bh: Blackhole): Unit = {
    val collection = new mutable.HashSet[Integer]
    for (i <- 0 until 1000) collection.add(i)
    for (i <- 0 until 1000) {
      val value = if (i % 4 == 0) -i else i
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