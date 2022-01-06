package com.adalbert.utils

import kotlin.random.Random

fun String.substringUntilLast(text: String): String {
    return this.substring(0 until this.lastIndexOf(text))
}

fun <T> List<T>.toProbabilityMap(probabilityPoints: Int = 1_000_000): MutableMap<T, IntRange> {
    val outcome = mutableMapOf<T, IntRange>()
    val distance = probabilityPoints / this.size
    for (i in this.indices)
        outcome[this[i]] = i * distance until (i + 1) * distance
    return outcome
}

fun <T> Map<T, IntRange>.scaleProbability(key: T, times: Double): MutableMap<T, IntRange> {
    val oldProbabilityPoints = this[key]?.let { it.last - it.first + 1 }
        ?: throw IllegalArgumentException("No key $key in given map!")
    val newProbabilityPoints = oldProbabilityPoints * times
    val difference = ((oldProbabilityPoints - newProbabilityPoints) / (this.size - 1)).toInt()
    var lastPoint = 0
    return this.mapValues {
        val currentDifference = if (it.key == key) newProbabilityPoints.toInt() else (it.value.last - it.value.first + 1) + difference
        val newProbabilityRange = lastPoint until lastPoint + currentDifference + 1
        lastPoint += currentDifference
        newProbabilityRange
    }.toMutableMap()
}

// TODO: This is bugged, probabilityPoints increase to sick values
fun <T> MutableMap<T, IntRange>.scaleProbabilityInPlace(key: T, times: Double) {
    val oldProbabilityPoints = this[key]?.let { it.last - it.first + 1 }
        ?: throw IllegalArgumentException("No key $key in given map!")
    val newProbabilityPoints = oldProbabilityPoints * times
    val difference = ((oldProbabilityPoints - newProbabilityPoints) / (this.size - 1)).toInt()
    var lastPoint = 0
    this.forEach {
        val currentDifference = if (it.key == key) newProbabilityPoints.toInt() else (it.value.last - it.value.first + 1) + difference
        val newProbabilityRange = lastPoint until lastPoint + currentDifference + 1
        lastPoint += currentDifference
        this[it.key] = newProbabilityRange
    }
}

fun <T> Map<T, IntRange>.random(): T? {
    val lastProbabilityPoint = this.maxByOrNull { it.value.last + 1 }?.value?.last?.plus(1)
        ?: throw IllegalStateException("No max probability in the map!")
    val randomInt = Random.nextInt(lastProbabilityPoint)
    return this.filter { it.value.contains(randomInt) }.keys.first()
}