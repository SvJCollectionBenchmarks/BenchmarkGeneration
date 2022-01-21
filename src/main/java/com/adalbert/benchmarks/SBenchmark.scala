package com.adalbert.benchmarks

import org.openjdk.jmh.annotations.{Benchmark, Fork, Scope, State}
import org.openjdk.jmh.infra.Blackhole
import scala.collection.mutable
import scala.collection.immutable
import java.util

import scala.collection.mutable.ArrayBuffer

@State(Scope.Benchmark)
class SBenchmark {

  private val elems = ArrayBuffer[Int](1, 2, 3, 4, 5)

  @Benchmark
  @Fork(1)
  def testMethodScala(bh: Blackhole): Unit = {
    val collection = new ArrayBuffer[Int]()
    collection.append(2)
    collection.addAll(elems)
    collection.subtractOne(3)
    bh.consume(collection.apply(3))
    collection.clear()
  }

  def dummy(): Unit = {
    var collection = new util.Vector[Int]()
  }
}
