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
            val supportedOperations = propertiesTree.getKeys("groups", groupName, "operations")?.toProbabilityMap()
                ?: throw IllegalStateException("Couldn't read operations provided by $groupName!")
            val chosenOperationSet = mutableSetOf<String>()
            val randomTries = 30
            (1 .. randomTries).forEach {
                val randomOperation = supportedOperations.random() ?: throw IllegalStateException()
                supportedOperations.scaleProbabilityInPlace(randomOperation, 2.0)
                chosenOperationSet.add(randomOperation)
            }
            println("Chosen ${chosenOperationSet.size} of ${supportedOperations.size} operations: $chosenOperationSet")
        }
    }
}