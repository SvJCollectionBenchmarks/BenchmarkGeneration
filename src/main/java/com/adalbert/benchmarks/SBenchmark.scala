package com.adalbert.benchmarks

import com.adalbert.benchmarks.Weird.Cell
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import java.util
import java.util.{ArrayList, Arrays, Comparator, HashMap, HashSet, List, Objects, Set}
import scala.collection.mutable
import scala.collection.mutable.Set

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
//
//  object Cell {
//    def livingCell(row: Int, column: Int) = new Weird.Cell(row, column, true)
//    def deadCell(row: Int, column: Int) = new Weird.Cell(row, column, false)
//  }
//  class Cell private(val row: Int, val column: Int, val isAlive: Boolean) {
//    override def equals(o: Any): Boolean = {
//      if (this == o) return true
//      if (o == null || (getClass ne o.getClass)) return false
//      val cell = o.asInstanceOf[Weird.Cell]
//      row == cell.row && column == cell.column && isAlive == cell.isAlive
//    }
//    override def hashCode: Int = Objects.hash(row, column, isAlive)
//    override def toString: String = "Cell{" + "row=" + row + ", column=" + column + ", isAlive=" + isAlive + '}'
//  }
//  private val startingPoint = util.Arrays.asList(
//    Cell.livingCell(0, 0), Cell.livingCell(1, 0), Cell.livingCell(2, 0),
//    Cell.livingCell(3, 1),
//    Cell.livingCell(1, 2), Cell.livingCell(2, 2),
//    Cell.livingCell(3, 3),
//    Cell.livingCell(0, 4), Cell.livingCell(1, 4), Cell.livingCell(2, 4)
//  )
//  private def addLivingCell(collection: mutable.Set[Weird.Cell], row: Int, column: Int): Unit = {
//    collection.remove(Cell.deadCell(row, column))
//    collection.add(Cell.livingCell(row, column))
//    for (i <- -1 until 2)
//      for (j <- -1 until 2)
//        if (!(i == 0 && j == 0) && !collection.contains(Cell.livingCell(row + i, column + j)))
//          collection.add(Cell.deadCell(row + i, column + j))
//  }
//  private def removeLivingCell(collection: mutable.Set[Weird.Cell], row: Int, column: Int): Unit = {
//    collection.remove(Cell.livingCell(row, column))
//    if (neighbours(collection, row, column) != 0) collection.add(Cell.deadCell(row, column))
//    for (i <- -1 until 2)
//      for (j <- -1 until 2)
//        if (!(i == 0 && j == 0) && neighbours(collection, row + i, column + j) == 0)
//          collection.remove(Cell.deadCell(row + i, column + j))
//  }
//  private def neighbours(collection: mutable.Set[Weird.Cell], row: Int, column: Int) = {
//    var neighbours = 0
//    for (i <- -1 until 2)
//      for (j <- -1 until 2)
//        if (!(i == 0 && j == 0) && collection.contains(Cell.livingCell(row + i, column + j)))
//          neighbours += 1
//    neighbours
//  }

}