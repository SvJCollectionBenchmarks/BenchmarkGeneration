package com.adalbert.benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.List;

public class Another {
    @Benchmark
    public void anotherBenchmarkInTheMaking (Blackhole bh) {
        java.util.ArrayList<Double> collection = new java.util.ArrayList<Double>();
        List<Double> samples = Arrays.asList(1.0, 0.75, 0.5, 0.25, 0.0);
        for (int i = 0; i < 8000 / samples.size(); i++) {
            bh.consume(i);
            collection.addAll(samples);
        }
        for (int i = 0; i < collection.size(); i++) {
            bh.consume(i);
            collection.set(i, collection.get(i) * 0.5);
        }
        while (collection.contains(0.0))
            collection.remove(0.0);
    }
}
