package com.adalbert.benchmarks

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import java.util
import java.util.{HashMap, HashSet}
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
    val collection = new mutable.HashMap[String, String]
    for (i <- 0 until 10000) {
      collection.put(String.format("Key %d", i), String.format("Value %d", i))
    }
    for (i <- 0 until 1000) {
      val value = if (i % 3 == 0) -i
      else i
      val mapKey = String.format("Key %d", value)
      val mapValue = String.format("New value %d", value)
      if (collection.contains(mapKey)) i % 2 match {
        case 0 =>
          collection.update(mapKey, mapValue)
        case 1 =>
          collection.remove(mapKey)
      }
      else collection.put(mapKey, mapValue)
    }
    bh.consume(collection.size)
    bh.consume(collection.keySet)
    bh.consume(collection.values)
    collection.clear()
  }

}