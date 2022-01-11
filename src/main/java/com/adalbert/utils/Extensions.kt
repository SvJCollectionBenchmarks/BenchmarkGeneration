package com.adalbert.utils

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.random.Random

fun String.substringUntilLast(text: String): String {
    return this.substring(0 until this.lastIndexOf(text))
}

fun String.substringFromLast(text: String): String {
    return this.substring(this.lastIndexOf(text) + 1)
}

fun String.times(times: Int): String {
    val bob = StringBuilder()
    (0 until times).forEach { _ -> bob.append(this) }
    return bob.toString()
}

fun <T> Set<T>.randomWithout(value: T): T {
    if (this.isEmpty()) throw IllegalStateException("Can't take random from empty set!")
    if (this.size == 1 && this.contains(value)) throw IllegalStateException("Can't take random without $value, because there is only that here!")
    var element : T
    do {
        element = this.random()
    } while (element == value)
    return element
}

fun <T> List<T>.toProbabilityMap(probabilityPoints: Int = 1_000_000): MutableMap<T, IntRange> {
    val outcome = mutableMapOf<T, IntRange>()
    val points = IntArray (this.size) { probabilityPoints / this.size }
    val differenceFromTotal = probabilityPoints - points.sum()
    (0 until abs(differenceFromTotal)).forEach { points[it % points.size] += differenceFromTotal.sign }
    var lastIndex = 0
    for (i in this.indices) {
        outcome[this[i]] = lastIndex until lastIndex + points[i]
        lastIndex += points[i]
    }
    return outcome
}

fun <T> MutableMap<T, IntRange>.scaleProbabilityInPlace(key: T, times: Double) {
    val totalProbabilityPoints = this.values.sumOf { it.last - it.first + 1 }
    val oldProbabilityPoints = this[key]?.let { it.last - it.first + 1 }
        ?: throw IllegalArgumentException("No key $key in given map!")
    val newProbabilityPoints: Int = min((oldProbabilityPoints * times).toInt(), totalProbabilityPoints - this.size + 1)
    val difference = ((oldProbabilityPoints - newProbabilityPoints).toDouble() / (this.size - 1)).toInt()
    val probabilityPoints = this.map { it.key to max(1, (it.value.last - it.value.first + difference + 1)) }
        .filter { it.first != key }.toMap().toMutableMap()
    val differenceFromTotal = totalProbabilityPoints - (probabilityPoints.values.sum() + newProbabilityPoints)
    (0 until abs(differenceFromTotal)).forEach { _ ->
        val randomKey = probabilityPoints.filter { differenceFromTotal.sign > 0 || it.value > 1 }.keys.random()
        probabilityPoints[randomKey] = probabilityPoints[randomKey]!! + differenceFromTotal.sign
    }
    var lastPoint = 0
    this.forEach {
        val currentDifference = if (it.key == key) newProbabilityPoints.toInt()
            else probabilityPoints[it.key] ?: throw IllegalStateException("A value ${it.key} wasn't mapped to new probability!")
        val newProbabilityRange = lastPoint until lastPoint + currentDifference
        lastPoint += currentDifference
        this[it.key] = newProbabilityRange
    }
}

fun <T> Map<T, IntRange>.random(): T? {
    val lastProbabilityPoint = this.maxByOrNull { it.value.last + 1 }?.value?.last?.plus(1)
        ?: throw IllegalStateException("No max probability in the map!")
    val randomInt = Random.nextInt(lastProbabilityPoint)
    return this.firstOrNull { it.contains(randomInt) }
}

fun <K, V> Map<K, V>.firstOrNull(predicate: (V) -> Boolean): K? {
    this.forEach { if (predicate(it.value)) return it.key }
    return null
}