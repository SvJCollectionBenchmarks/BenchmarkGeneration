package com.adalbert.benchmarks

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import java.util
import java.util.{ArrayList, HashMap, HashSet}
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
    val collection = new util.ArrayList[Double]
    for (i <- 0 until 10000) {
      collection.add(Math.sin(i / 0.01))
    }
    for (i <- 0 until 3000) {
      val index = (i * 3000) % collection.size
      i % 3 match {
        case 0 =>
          collection.add(index, 0.0)
        case 1 =>
          collection.set(index, 1.0)
        case 2 =>
          collection.remove(index)
      }
    }
  }

}