package com.adalbert.utils

fun String.substringUntilLast(text: String): String {
    return this.substring(0 until this.lastIndexOf(text))
}

fun <T> List<T>.toProbabilityMap(scale: Int = 1_000_000): Map<T, IntRange> {
    val outcome = mutableMapOf<T, IntRange>()
    val distance = scale / this.size
    for (i in this.indices)
        outcome[this[i]] = i * distance until i * (distance + 1)
    return outcome
}

fun <T> Map<T, IntRange>.scaleProbability(key: T, times: Double): Map<T, IntRange> {
    val outcome = mutableMapOf<T, IntRange>()
    val scale = this.maxByOrNull { it.value.last + 1 }?.value?.last?.plus(1)?.toDouble()
        ?: throw IllegalStateException("No max probability in the map!")
    val oldProbabilityPoints = this[key]?.let { it.last - it.first + 1 }
        ?: throw IllegalArgumentException("No key $key in given map!")
    val newProbability = oldProbabilityPoints * times
    // TODO: Do something here!
    return outcome
}