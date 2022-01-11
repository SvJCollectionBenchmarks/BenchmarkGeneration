package com.adalbert.generation

import com.adalbert.utils.Tree
import com.adalbert.utils.substringFromLast
import com.adalbert.utils.times
import kotlin.random.Random

object ArgumentsGenerator {

    private data class Argument(var name: String, var type: String)

    fun generateArguments(group: String, profiles: List<String>, operation: String, propertiesTree: Tree): Map<String, String> {
        val profile = propertiesTree.getFirstMatchingKey(profiles, "groups", group, "operations", operation)
        val argsMappings = propertiesTree.getMappings("groups", group, "operations", operation, profile, "args")
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
            .associateWith { matchVariableWithRandom(it, group, profile, propertiesTree) }
        arguments.forEach { if (typeVariables.containsKey(it.type)) it.type = typeVariables[it.type]!! }
        return mapOf()
    }

    private fun matchVariableWithRandom(text: String, group: String, profile: String, propertiesTree: Tree): String {
        if (!text.contains("$")) return text
        val variableName = text.substringFromLast("$")
        return propertiesTree.getValues("groups", group, "variables", profile, variableName)?.random()
            ?: throw IllegalStateException("Couldn't get variable $text for group $group!")
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