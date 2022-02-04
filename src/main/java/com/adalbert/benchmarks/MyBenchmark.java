/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.adalbert.benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import scala.math.Ordering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;


@State(Scope.Benchmark)
public class MyBenchmark {

    private final Collection<? extends Integer> elems = Arrays.asList(1, 2, 3, 4, 5);

    @Benchmark
    @Fork(1)
    public void testJHashMap(Blackhole bh) {
        java.util.HashMap<Integer, java.util.List<Integer>> collection = new java.util.HashMap<>();
        for (int i = 2; i < 100; i++) {
            boolean wasFound = false;
            Iterator<Integer> iterator = collection.keySet().iterator();
            while (iterator.hasNext() && !wasFound) {
                Integer key = iterator.next();
                wasFound = i % key == 0;
                if (wasFound) collection.get(key).add(i);
            }
            if (!wasFound) collection.put(i, new ArrayList<Integer>());
        }
        bh.consume(collection.keySet());
    }

    @Benchmark
    @Fork(1)
    public void testJLinkedMap(Blackhole bh) {
        java.util.LinkedHashMap<Integer, java.util.List<Integer>> collection = new java.util.LinkedHashMap<>();
        for (int i = 2; i < 100; i++) {
            boolean wasFound = false;
            Iterator<Integer> iterator = collection.keySet().iterator();
            while (iterator.hasNext() && !wasFound) {
                Integer key = iterator.next();
                wasFound = i % key == 0;
                if (wasFound) collection.get(key).add(i);
            }
            if (!wasFound) collection.put(i, new ArrayList<Integer>());
        }
        bh.consume(collection.keySet());
    }

    @Benchmark
    @Fork(1)
    public void testSHashMap(Blackhole bh) {
        scala.collection.mutable.HashMap<Integer, java.util.List<Integer>> collection = new scala.collection.mutable.HashMap<>();
        for (int i = 2; i < 100; i++) {
            boolean wasFound = false;
            scala.collection.Iterator<Integer> iterator = collection.keySet().iterator();
            while (iterator.hasNext() && !wasFound) {
                Integer key = iterator.next();
                wasFound = i % key == 0;
                if (wasFound) collection.apply(key).add(i);
            }
            if (!wasFound) collection.update(i, new ArrayList<Integer>());
        }
        bh.consume(collection.keySet());
    }

    @Benchmark
    @Fork(1)
    public void testSLinkedMap(Blackhole bh) {
        scala.collection.mutable.LinkedHashMap<Integer, java.util.List<Integer>> collection = new scala.collection.mutable.LinkedHashMap<>();
        for (int i = 2; i < 100; i++) {
            boolean wasFound = false;
            scala.collection.Iterator<Integer> iterator = collection.keySet().iterator();
            while (iterator.hasNext() && !wasFound) {
                Integer key = iterator.next();
                wasFound = i % key == 0;
                if (wasFound) collection.apply(key).add(i);
            }
            if (!wasFound) collection.update(i, new ArrayList<Integer>());
        }
        bh.consume(collection.keySet());
    }

    @Benchmark
    @Fork(1)
    public void testJTreeMap(Blackhole bh) {
        java.util.TreeMap<Integer, java.util.List<Integer>> collection = new java.util.TreeMap<>();
        for (int i = 2; i < 100; i++) {
            boolean wasFound = false;
            java.util.Iterator<Integer> iterator = collection.keySet().iterator();
            while (iterator.hasNext() && !wasFound) {
                Integer key = iterator.next();
                wasFound = i % key == 0;
                if (wasFound) collection.get(key).add(i);
            }
            if (!wasFound) collection.put(i, new ArrayList<Integer>());
        }
        bh.consume(collection.keySet());
    }

    @Benchmark
    @Fork(1)
    public void testSTreeMap(Blackhole bh) {
        scala.collection.mutable.TreeMap<Integer, java.util.List<Integer>> collection = new scala.collection.mutable.TreeMap<>(new Ordering<Integer>() { public int compare(Integer x, Integer y) { return x.compareTo(y); }});
        for (int i = 2; i < 100; i++) {
            boolean wasFound = false;
            scala.collection.Iterator<Integer> iterator = collection.keySet().iterator();
            while (iterator.hasNext() && !wasFound) {
                Integer key = iterator.next();
                wasFound = i % key == 0;
                if (wasFound) collection.apply(key).add(i);
            }
            if (!wasFound) collection.update(i, new ArrayList<Integer>());
        }
        bh.consume(collection.keySet());
    }

}
