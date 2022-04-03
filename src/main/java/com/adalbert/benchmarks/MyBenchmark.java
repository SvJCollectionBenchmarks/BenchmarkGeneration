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

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

@State(Scope.Benchmark)
public class MyBenchmark {

//    private static class Node {
//        private int id;
//        private List<Integer> dependant;
//        public Node(int id) {
//            this.id = id;
//            this.dependant = new ArrayList<>();
//        }
//        public void addDependency(int dependency) {
//            this.dependant.add(dependency);
//        }
//    }
//
//    @Benchmark
//    @Fork(1)
//    @Measurement(time = 5)
//    @Warmup(time = 5)
//    public void testMethodFirst(Blackhole bh) {
//        HashSet<Node> collection = new HashSet<>();
//        for (int i = 0; i < 1000; i++) {
//            boolean toAdd = true;
//            Iterator<Node> iter = collection.iterator();
//            while (iter.hasNext()) {
//                Node primes = iter.next();
//                if (i % primes.id == 0) {
//                    toAdd = false;
//                    primes.dependant.add(i);
//                }
//            }
//            if (toAdd) collection.add(new Node(i));
//        }
//        System.out.println("aaa");
//    }

    @Benchmark
    @Fork(1)
    public void testArrayList(Blackhole bh) {
        ArrayList<Integer> collection = new ArrayList<>();
        ListIterator<Integer> iter = null;
        int value = 0;
        for (int i = 0; i < 1000000; i++) {
            value = i / (i % 3 + 1);
            iter = collection.listIterator();
            while (iter.hasNext())
                if (iter.next() >= value)
                    iter.add(value);
        }
    }

    @Benchmark
    @Fork(1)
    public void testLinkedList(Blackhole bh) {
        LinkedList<Integer> collection = new LinkedList<>();
        ListIterator<Integer> iter = null;
        int value = 0;
        for (int i = 0; i < 1000000; i++) {
            value = i / (i % 3 + 1);
            iter = collection.listIterator();
            while (iter.hasNext())
                if (iter.next() >= value)
                    iter.add(value);
        }
    }


}
