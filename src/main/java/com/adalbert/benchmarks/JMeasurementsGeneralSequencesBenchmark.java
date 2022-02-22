package com.adalbert.benchmarks;

import java.util.*;
import scala.collection.immutable.*;
import scala.collection.mutable.*;
import scala.math.Ordering;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
public class JMeasurementsGeneralSequencesBenchmark {
    @Benchmark
    @Measurement(iterations = 6)
    public void testJArraySequence(Blackhole bh) {
        java.util.ArrayList<Double> collection = new java.util.ArrayList<>();;
        for (int i = 0; i < 1000; i++) collection.add(i / 2.0);;
        for (int i = 0; i < 1000; i++) if (i % 10 < 4) collection.set(i, -1.0);;
        bh.consume(collection.get(100)); collection.remove(100);;
        bh.consume(collection.get(900)); collection.remove(900);;
        bh.consume(collection.get(200)); collection.remove(200);;
        bh.consume(collection.get(800)); collection.remove(800);;
        bh.consume(collection.get(300)); collection.remove(300);;
        bh.consume(collection.get(700)); collection.remove(700);;
    }
    @Benchmark
    @Measurement(iterations = 6)
    public void testJLinkedSequence(Blackhole bh) {
        java.util.LinkedList<Double> collection = new java.util.LinkedList<>();;
        for (int i = 0; i < 1000; i++) collection.add(i / 2.0);;
        for (int i = 0; i < 1000; i++) if (i % 10 < 4) collection.set(i, -1.0);;
        bh.consume(collection.get(100)); collection.remove(100);;
        bh.consume(collection.get(900)); collection.remove(900);;
        bh.consume(collection.get(200)); collection.remove(200);;
        bh.consume(collection.get(800)); collection.remove(800);;
        bh.consume(collection.get(300)); collection.remove(300);;
        bh.consume(collection.get(700)); collection.remove(700);;
    }
    @Benchmark
    @Measurement(iterations = 6)
    public void testJVectorSequence(Blackhole bh) {
        java.util.Vector<Double> collection = new java.util.Vector<>();;
        for (int i = 0; i < 1000; i++) collection.add(i / 2.0);;
        for (int i = 0; i < 1000; i++) if (i % 10 < 4) collection.set(i, -1.0);;
        bh.consume(collection.get(100)); collection.remove(100);;
        bh.consume(collection.get(900)); collection.remove(900);;
        bh.consume(collection.get(200)); collection.remove(200);;
        bh.consume(collection.get(800)); collection.remove(800);;
        bh.consume(collection.get(300)); collection.remove(300);;
        bh.consume(collection.get(700)); collection.remove(700);;
    }
}