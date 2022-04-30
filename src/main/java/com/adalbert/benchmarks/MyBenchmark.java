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
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.Objects;
import java.util.TreeSet;

@State(Scope.Benchmark)
public class MyBenchmark {

    static class Task implements Comparable<Task> {
        private final double priority;
        private final int time;
        private final String command;

        public Task(double priority, int time, String command) {
            this.priority = priority;
            this.time = time;
            this.command = command;
        }

        public double getPriority() { return priority; }
        public String getCommand() { return command; }
        public int getTime() { return time; }

        @Override
        public int compareTo(@NotNull Task o) {
            return -(int)((this.priority - o.priority)*1000000);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Task task = (Task) o;
            return priority == task.priority && time == task.time && Objects.equals(command, task.command);
        }

        @Override
        public int hashCode() {
            return Objects.hash(priority, time, command);
        }

        @Override
        public String toString() {
            return "Task{" +
                    "priority=" + priority +
                    ", time=" + time +
                    ", command='" + command + '\'' +
                    '}';
        }
    }

//    @Benchmark
//    @Fork(1)
//    @Measurement(time=1)
//    @Warmup(time=1)
//    public void testArrayList(Blackhole bh) {
//        TreeSet<Task> collection = new TreeSet<>();
//        for (int i = 0; i < 5; i++)
//            collection.add(new Task((int) Math.floor(Math.sin(i)), "ABC"));
//        System.out.println(collection);
//    }

    public static void main(String[] args) {
        TreeSet<Task> collection = new TreeSet<>();
        int calcPower = 0;
        Task currentTask = null;
        for (int i = 0; i < 1000; i++) {
            switch (i % 2) {
                case 0:  collection.add(new Task(1000 * Math.sin(i), i%3 + i%4 + i%5, "Command A")); break;
                case 1:  collection.add(new Task(1000 * Math.cos(i), i%4 + i%6 + i%7, "Command B")); break;
            }
            if (currentTask == null)
                currentTask = collection.iterator().next();
            if (currentTask.time <= calcPower) {
                calcPower -= currentTask.time;
                collection.remove(currentTask);
                currentTask = null;
            }
            calcPower += 5;
        }
        System.out.println(collection.size());
    }

}
