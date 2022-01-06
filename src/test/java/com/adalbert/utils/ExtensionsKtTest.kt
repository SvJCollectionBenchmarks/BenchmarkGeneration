package com.adalbert.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.math.abs

internal class ExtensionsKtTest {

    @Test
    fun substringUntilLast() {
        assertEquals("my.file", "my.file.txt".substringUntilLast("."))
    }

    @Test
    fun toProbabilityMap() {
        val probabilityMap = listOf(1,2,3,4,5).toProbabilityMap()
        probabilityMap.forEach { assertEquals(200_000, it.value.last - it.value.first + 1) }
    }

    @Test
    fun scaleProbability() {
        val probabilityMap = listOf(1,2,3,4,5).toProbabilityMap().scaleProbability(3, 2.0)
        probabilityMap.forEach { assertEquals(if (it.key == 3) 400_000 else 150_000, it.value.last - it.value.first) }
    }

    @Test
    fun random() {
        val tries = 100_000
        val probabilityMap = listOf(0,1,2,3,4).toProbabilityMap()
        val counters = IntArray(5) { 0 }
        (0 until tries).forEach { _ -> counters[probabilityMap.random()!!] += 1 }
        counters.forEach { assertTrue(abs(it - 0.2 * 100_000) <= 0.005 * tries) }
    }
}