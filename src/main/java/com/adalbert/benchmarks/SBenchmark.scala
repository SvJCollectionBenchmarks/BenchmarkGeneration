package com.adalbert.benchmarks

import org.openjdk.jmh.annotations.{Benchmark, Fork, Measurement, Scope, State, Warmup}
import org.openjdk.jmh.infra.Blackhole

import scala.collection.mutable
import scala.collection.immutable
import java.util
import java.util.{ArrayList, Arrays, List}
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.reflect.ClassTag

@State(Scope.Benchmark)
class SBenchmark {

  private val elems = ArrayBuffer[Int](1, 2, 3, 4, 5)

  @Benchmark
  @Fork(1)
  @Measurement(iterations = 1)
  @Warmup(iterations = 1)
  def testMethodScala(bh: Blackhole): Unit = {
    val collection = ArrayBuffer[Double]()
    val samples = ArrayBuffer[Double](1.0, 0.75, 0.5, 0.25, 0.0)
    for (i <- 0 until 8000 / samples.size) {
      bh.consume(i)
      collection.addAll(samples)
    }
    for (i <- collection.indices) {
      bh.consume(i)
      collection.update(i, collection.apply(i) * 0.5)
    }
    while (collection.contains(0.0))
      collection.subtractOne(0.0)
  }

}

object Main extends App {
  var collection = mutable.HashSet[Integer]()
  val set3 = mutable.HashSet[Integer]()
  val set5 = mutable.HashSet[Integer]()
  val set7 = mutable.HashSet[Integer]()
  for (i <- 0 until 1000) {
    if (i % 3 == 0) set3.add(i)
    if (i % 5 == 0) set5.add(i)
    if (i % 7 == 0) set7.add(i)
    if (i % 100 == 0)  {
      collection = set3.union(set5).union(set7)
      collection = collection.diff(set7).diff(set5)
      collection = collection.intersect(set3)
    }
  }
}