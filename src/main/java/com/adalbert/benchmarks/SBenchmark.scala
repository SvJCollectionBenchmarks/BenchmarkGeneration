package com.adalbert.benchmarks

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import java.util.Objects
import scala.collection.mutable

class Message(var id: Long, var message: String)

class Task(val priority: Double, val time: Int) extends Ordered[Task] {
  def getTime: Int = time
  override def hashCode: Int =
    Objects.hash(priority, time)
  override def compare(that: Task): Int =
    -((this.priority - that.priority) * 1000000).toInt
  override def equals(o: Any): Boolean = {
    if (o == null || (getClass ne o.getClass)) return false
    val task = o.asInstanceOf[Task]
    priority == task.priority && time == task.time
  }
}

@State(Scope.Benchmark)
class SBenchmark {

  @Benchmark
  @Fork(1)
  @Measurement(iterations = 1)
  @Warmup(iterations = 1)
  def testMethodScala(bh: Blackhole): Unit = {
    val collection = new mutable.TreeSet[Task]
    var calcPower = 0
    var currentTask: Task = null
    for (i <- 0 until 1000) {
      val priority = if (i % 2 == 0) 1000 * Math.sin(i)
      else 1000 * Math.cos(i)
      val time = if (i % 2 == 0) i % 3 + i % 4 + i % 5
      else i % 4 + i % 6 + i % 7
      collection.add(new Task(priority, time))
      if (currentTask == null) currentTask = collection.iterator.next
      if (currentTask.getTime <= calcPower) {
        calcPower -= currentTask.getTime
        collection.remove(currentTask)
        currentTask = null
      }
      calcPower += 5
    }
  }

}