package com.adalbert.benchmarks

import org.openjdk.jmh.annotations.{Benchmark, Fork, Measurement, Scope, State, Warmup}
import org.openjdk.jmh.infra.Blackhole

import scala.collection.mutable
import scala.collection.immutable
import java.util
import java.util.ArrayList
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
    val collection = new util.ArrayList[Double]
    for (i <- 0 until 1000) {
      collection.add(i / 2.0)
    }
    for (i <- 0 until 1000) {
      if (i % 10 < 4) collection.set(i, -1.0)
    }
    bh.consume(collection.get(100))
    collection.remove(100)
    bh.consume(collection.get(900))
    collection.remove(900)
    bh.consume(collection.get(200))
    collection.remove(200)
    bh.consume(collection.get(800))
    collection.remove(800)
    bh.consume(collection.get(300))
    collection.remove(300)
    bh.consume(collection.get(700))
    collection.remove(700)
  }

}

object Main extends App {
  val classTag: ClassTag[Long] = ClassTag(Long.getClass)
  val protoArgument: util.Collection[String] = new util.ArrayList()
  (0 until 10).foreach(it => protoArgument.add(it.toString))
  val targetArgument = protoArgument.asScala
  println(targetArgument)
}