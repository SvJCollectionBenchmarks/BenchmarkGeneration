package com.adalbert.generation

import com.adalbert.utils.Tree
import com.adalbert.utils.times
import kotlin.random.Random

object ArgumentsGenerator {

    private data class Argument(val name: String, val type: String)

    fun generateArguments(group: String, profiles: List<String>, operation: String, propertiesTree: Tree): Map<String, String> {
        val argsMappings = propertiesTree.getMatchingTree(profiles, "groups", group, "operations", operation).getMappings("args")
        val arguments = argsMappings?.let { argsInner -> (0 until argsInner.size / 2).map { index ->
            if (argsInner[2 * index].first == "name") Argument(argsInner[2 * index].second.first(), argsInner[(2 * index) + 1].second.first())
            else if (argsInner[(2 * index) + 1].first == "name") Argument(argsInner[(2 * index) + 1].second.first(), argsInner[2 * index].second.first())
            else throw IllegalStateException("Wrong args mapping found!")
        }}

        return mapOf()
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