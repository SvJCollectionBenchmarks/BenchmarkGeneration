package com.adalbert.generation

import com.adalbert.utils.*
import java.io.File
import java.net.URLDecoder

private const val profilesNumber = 1

fun main() {
    val resourcesUri = URLDecoder.decode(Tree("", mutableListOf()).javaClass.getResource("/")?.path, "UTF-8")
        ?: throw IllegalStateException("Couldn't find main resources folder")

    val groupsFiles = File("$resourcesUri/groups").listFiles()
        ?.filter { it.extension == "json" }
        ?: throw IllegalStateException("Couldn't load groups")

    val propertiesTree = Tree("root", mutableListOf(
        Tree("languages", mutableListOf(), mutableListOf("java", "scala")),
        Tree("groups", groupsFiles.map { file -> JSONTreeParser.parseJsonFile(file) }.toMutableList()),
    ))

    (0 until profilesNumber).forEach {
        val groups = propertiesTree.getKeys("groups")
        groups?.forEach { groupName ->
            val operations: MutableMap<String, IntRange> = propertiesTree.getKeys("groups", groupName, "operations")?.toProbabilityMap()
                ?: throw IllegalStateException("Couldn't read operations provided by $groupName!")
            // WARN: The famous "can't measure nothing" paradigm
            operations.remove("clear")
            val chosenOperations = mutableListOf<String>()
            (1 .. 30).forEach { _ ->
                val randomOperation = operations.random() ?: throw IllegalStateException()
                operations.scaleProbabilityInPlace(randomOperation, 1.0)
                chosenOperations.add(randomOperation)
            }
            val generated = propertiesTree.getKeys("groups", groupName, "generated")
            generated?.forEach { generatedName ->
                val profiles = mutableListOf(generatedName)
                val defaultProfile = propertiesTree.getValues("groups", groupName, "generated", generatedName)
                    ?: throw IllegalStateException("Couldn't get default profile reading for $generatedName!")
                profiles.addAll(defaultProfile)
                val operationsWithArguments = chosenOperations.associateWith { ArgumentsGenerator.generateArguments(groupName, profiles, it, propertiesTree) }
                println(operationsWithArguments)
            }

        }
    }
}


