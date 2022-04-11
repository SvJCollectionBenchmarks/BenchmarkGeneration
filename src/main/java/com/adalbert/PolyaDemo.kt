package com.adalbert

import com.adalbert.utils.*

fun main() {
//    val randomCount = 100
//    val startingPolyaMultiplier = 1.5
//    val options = (1 .. 10).map { "Option $it" }.toProbabilityMap()
//    val chosenOptions = mutableListOf<String>()
//    (1 .. randomCount).forEach { _ ->
//        val randomOption = options.random() ?: throw IllegalStateException()
//        val optionCount = chosenOptions.count { it == randomOption }
//        val realMultiplier =  (1.0 + (startingPolyaMultiplier - 1.0) / (optionCount + 1))
//        options.scaleProbabilityInPlace(randomOption, realMultiplier)
//        chosenOptions.add(randomOption)
//    }
//    println("Using ${chosenOptions.distinct().size} out of ${options.size} options")
//    println("Used options are: ${chosenOptions.itemsPercentage()}")
//    options.displayProbabilityMap()

    val randomCount = 100
    val startingPolyaMultiplier = 1.4
    val options = ProbabilityList.of((1 .. 10).map { "Option $it" })
    val chosenOptions = mutableListOf<String>()
    (1 .. randomCount).forEach { _ ->
        val randomOption = options.random()
        val optionCount = chosenOptions.count { it == randomOption }
        val realMultiplier = (1.0 + (startingPolyaMultiplier - 1.0) / (optionCount + 1))
        options.scaleProbability(randomOption, realMultiplier)
        chosenOptions.add(randomOption)
    }
    println("Using ${chosenOptions.distinct().size} out of ${options.size()} options")
    println("Used options are: ${chosenOptions.itemsPercentage()}")
    println(chosenOptions)
    println(options)
}