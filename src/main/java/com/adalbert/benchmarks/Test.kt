package com.adalbert.benchmarks

class Sample(val value: Int, val isTrue: Boolean) {
    override fun hashCode(): Int {
        return value.hashCode();
    }

    override fun equals(other: Any?): Boolean {
        if (other is Sample)
            return this.value == other.value
        return false
    }
}

fun main() {
    val set = setOf(Sample(0, true), Sample(0, false))
    println(set)
}