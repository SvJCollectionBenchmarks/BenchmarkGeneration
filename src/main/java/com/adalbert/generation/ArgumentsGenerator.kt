package com.adalbert.generation

import com.adalbert.utils.*
import kotlin.random.Random

object ArgumentsGenerator {

    private data class Argument(var name: String, var type: String)

    fun generateArguments(group: String, profiles: List<String>, operation: String, propertiesTree: Tree): Map<String, String> {
        val operationProfile = propertiesTree.getFirstMatchingKey(profiles, "groups", group, "operations", operation)
        val argsMappings = propertiesTree.getMappings("groups", group, "operations", operation, operationProfile, "args")
        val arguments = argsMappings?.let { argsInner -> (0 until argsInner.size / 2).map { index ->
            if (argsInner[2 * index].first == "name")
                Argument (argsInner[2 * index].second.first(), argsInner[(2 * index) + 1].second.first())
            else if (argsInner[(2 * index) + 1].first == "name")
                Argument (argsInner[(2 * index) + 1].second.first(), argsInner[2 * index].second.first())
            else throw IllegalStateException("Wrong args mapping found!")
        }}?.toMutableList() ?: throw IllegalStateException("Didn't find argument mappings for operation $operation from $group group!")
        val typeVariables = arguments
            .filter { it.type.contains("$") }
            .map { it.type }.toSet()
            .associateWith { matchVariableWithRandom(it, group, profiles, propertiesTree) }
        arguments.forEach { if (typeVariables.containsKey(it.type)) it.type = typeVariables[it.type]!! }
        // TODO: If mapping from type to random value is null, we should throw an exception
        return arguments.associate { it.name to (randomValuesGeneration[it.type]?.let { it1 -> it1() } ?: "${it.type} not mapped")  }
    }

    private fun matchVariableWithRandom(text: String, group: String, profiles: List<String>, propertiesTree: Tree): String {
        if (!text.contains("$")) return text
        val variableName = text.substringFromLast("$").substringUntil('[', ']', '<', '>')
        val variableProfile = propertiesTree.getFirstMatchingKey(profiles, "groups", group, "variables")
        val randomizedValue = propertiesTree.getValues("groups", group, "variables", variableProfile, variableName)?.random()
            ?: throw IllegalStateException("Couldn't get variable $text for group $group!")
        return text.replace("\$$variableName", randomizedValue)
    }

    private val randomValuesGeneration = mutableMapOf<String, () -> String>().apply {
        this.putAll(listOf("int", "Int", "Integer").associateWith { { "${Random.nextInt(1, 10)}" } })
        this.putAll(listOf("float", "Float").associateWith { { "${Random.nextDouble(10.0)}" } })
        this.putAll(listOf("double", "Double").associateWith { { "${Random.nextDouble(10.0)}" } })
        this.putAll(listOf("boolean", "Boolean").associateWith { { "${Random.nextBoolean()}" } })
        this.putAll(listOf("long", "Long").associateWith { { "${Random.nextLong(Long.MAX_VALUE)}" } })
        this["String"] = { ('a' .. 'z').toList().randomTimes(Random.nextInt(5, 20)).joinToString("") }
    }

}