package com.adalbert.benchmarks

import org.openjdk.jmh.annotations.{Benchmark, Fork, Scope, State}
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
  def testMethodScala(bh: Blackhole): Unit = {
    val collection = new ArrayBuffer[Int]()
    collection.append(2)
    collection.addAll(elems)
    collection.subtractOne(3)
    bh.consume(collection.apply(3))
    collection.clear()
  }

}

object Main extends App {
  val classTag: ClassTag[Long] = ClassTag(Long.getClass)
  val protoArgument: util.Collection[String] = new util.ArrayList()
  (0 until 10).foreach(it => protoArgument.add(it.toString))
  val targetArgument = protoArgument.asScala
  println(targetArgument)
}