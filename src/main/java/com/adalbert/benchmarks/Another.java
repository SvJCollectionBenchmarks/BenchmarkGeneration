package com.adalbert.benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

public class Another {
    @Benchmark
    @Warmup(time = 1)
    @Measurement(time = 1)
    public void arrayListQueue (Blackhole bh) {
        java.util.ArrayList<Double> collection = new java.util.ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            if (i % 5 == 0) collection.add(0, Math.sin(i * 0.01));
            else if (i % 5 == 1) collection.add(0, Math.sin(i * 0.01));
            else if (i % 5 == 2) collection.remove(0);
            else if (i % 5 == 3) collection.add(0, Math.sin(i * 0.01));
            else collection.remove(collection.size() - 1);
        }
    }

    @Benchmark
    @Warmup(time = 1)
    @Measurement(time = 1)
    public void linkedListQueue (Blackhole bh) {
        java.util.LinkedList<Double> collection = new java.util.LinkedList<>();
        for (int i = 0; i < 1000; i++) {
            if (i % 5 == 0) collection.add(0, Math.sin(i * 0.01));
            else if (i % 5 == 1) collection.add(0, Math.sin(i * 0.01));
            else if (i % 5 == 2) collection.remove(0);
            else if (i % 5 == 3) collection.add(0, Math.sin(i * 0.01));
            else collection.remove(collection.size() - 1);
        }
    }
}
