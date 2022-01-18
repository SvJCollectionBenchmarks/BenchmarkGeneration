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
        println("$group $operation $operationProfile $arguments")
        return mapOf()
    }

    private fun matchVariableWithRandom(text: String, group: String, profiles: List<String>, propertiesTree: Tree): String {
        if (!text.contains("$")) return text
        val variableName = text.substringFromLast("$").substringUntil('[', ']', '<', '>')
        val variableProfile = propertiesTree.getFirstMatchingKey(profiles, "groups", group, "variables")
        val randomizedValue = propertiesTree.getValues("groups", group, "variables", variableProfile, variableName)?.random()
            ?: throw IllegalStateException("Couldn't get variable $text for group $group!")
        return text.replace("\$$variableName", randomizedValue)
    }

    val randomValuesGeneration = mapOf<String, () -> String>(
        "int" to { Random.nextInt(1, 1000).toString() },
        "Integer" to { Random.nextInt(1, 1000).toString() },
        "String" to {
            Random.nextInt(65, 91).toChar().toString()
                .times(Random.nextInt(1, 10))
        }
    )

}