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

import org.jetbrains.annotations.NotNull;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import scala.collection.mutable.ListBuffer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.TreeSet;

@State(Scope.Benchmark)
public class MyBenchmark {

    class Message {
        long id;
        String message;
        public Message(long id, String message) {
            this.id = id;
            this.message = message;
        }
    }

    @Benchmark
    @Fork(1)
    @Measurement(time=1)
    @Warmup(time=1)
    public void testArrayList(Blackhole bh) {
        ListBuffer<Message> collection = new ListBuffer<>();
        for (int i = 0; i < 10000; i++) {
            double value = Math.sin(i) * (i % 5);
            if (value > -0.25) collection.append(new Message(i, "Message text"));
            if (i % 3 == 0 && !collection.isEmpty()) collection.remove(0);

            int halfSize = collection.size() / 2;
            if (value < -3.5 && !collection.isEmpty()) collection.update(halfSize, new Message(halfSize, "Another message text"));
            if (value < -2.5 && !collection.isEmpty()) collection.remove(collection.size() / 2);
        }
    }

    @Benchmark
    @Fork(1)
    @Measurement(time=1)
    @Warmup(time=1)
    public void testLinkedList(Blackhole bh) {
        LinkedList<Message> collection = new LinkedList<>();
        for (int i = 0; i < 10000; i++) {
            double value = Math.sin(i) * (i % 5);
            if (value > -0.25) collection.addLast(new Message(i, "Message text"));
            if (i % 3 == 0 && !collection.isEmpty()) collection.removeFirst();

            int halfSize = collection.size() / 2;
            if (value < -3.5 && !collection.isEmpty()) collection.set(halfSize, new Message(halfSize, "Another message text"));
            if (value < -2.5 && !collection.isEmpty()) collection.remove(collection.size() / 2);
        }
    }
//
//    public static void main(String[] args) {
//        LinkedList<Message> collection = new LinkedList<>();
//        for (int i = 0; i < 10000; i++) {
//            double value = Math.sin(i) * (i % 5);
//            if (value > -0.25) collection.addLast(new Message(i, "Message text"));
//            if (i % 3 == 0 && !collection.isEmpty()) collection.removeFirst();
//
//            int halfSize = collection.size() / 2;
//            if (value < -3.5 && !collection.isEmpty()) collection.set(halfSize, new Message(halfSize, "Another message text"));
//            else if (value < -2.5 && !collection.isEmpty()) collection.remove(collection.size() / 2);
//        }
//        System.out.println(collection.size());
//    }

}
