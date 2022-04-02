package com.adalbert.utils

import kotlin.random.Random

class ProbabilityList<T> private constructor(list: List<T>, points: Int) {

    private var probabilityMax: Long = (list.size * points).toLong()
    private val backingList: MutableList<Pair<T, LongRange>> = list
        .mapIndexed { index, value -> value to (index * points.toLong() until (index + 1) * points) }
        .toMutableList()

    companion object {
        fun <T>of(list: List<T>, points: Int = 10000): ProbabilityList<T> {
            return ProbabilityList(list, points)
        }
    }

    fun scaleProbability(element: T, times: Double) {
        val elementIndex = backingList.map { it.first }.indexOf(element)
        if (elementIndex == -1) throw IllegalArgumentException()

        val elementRange = backingList[elementIndex].second
        val newPoints = ((elementRange.last - elementRange.first + 1) * times).toInt()
        val newRange = elementRange.first until elementRange.first + newPoints
        backingList[elementIndex] = backingList[elementIndex].first to newRange

        val pointsDifference = newPoints - (elementRange.last - elementRange.first + 1)
        this.probabilityMax += pointsDifference
        for (i in elementIndex + 1 until backingList.size) {
            val oldRange = backingList[i].second
            val updatedRange = oldRange.first + pointsDifference .. oldRange.last + pointsDifference
            backingList[i] = backingList[i].first to updatedRange
        }
    }

    fun random(): T {
        val random = Random.nextLong(probabilityMax)
        return backingList.first { it.second.contains(random) }.first
    }

    fun size(): Int {
        return backingList.size
    }

    override fun toString(): String {
        return backingList.joinToString { "${listOf(it.first, it.second, String.format("%.2f%%", it.second.size() * 100.0 / probabilityMax))}\n" }
    }

}