package com.adalbert.benchmarks

import org.openjdk.jmh.annotations.{Benchmark, Fork, Measurement, Scope, State, Warmup}
import org.openjdk.jmh.infra.Blackhole

import scala.collection.mutable
import scala.collection.immutable
import java.util
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
    var collection = mutable.TreeMap[Integer, util.List[Integer]]();
    for (i <- 2 until 100) {
      var wasFound = false;
      val iterator = collection.keySet.iterator;
      while (iterator.hasNext && !wasFound) {
        val key = iterator.next()
        wasFound = i % key == 0;
        if (wasFound) collection.apply(key).add(i);
      }
      if (!wasFound) collection.update(i, new util.ArrayList[Integer]());
    }
    bh.consume(collection.keySet);
  }

}

object Main extends App {
  val classTag: ClassTag[Long] = ClassTag(Long.getClass)
  val protoArgument: util.Collection[String] = new util.ArrayList()
  (0 until 10).foreach(it => protoArgument.add(it.toString))
  val targetArgument = protoArgument.asScala
  println(targetArgument)
}