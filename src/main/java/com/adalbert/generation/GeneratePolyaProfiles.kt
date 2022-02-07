package com.adalbert.generation

import com.adalbert.generation.ArgumentsGenerator.generateArgumentsForProfile
import com.adalbert.generation.ArgumentsGenerator.mapArgumentsToProfile
import com.adalbert.utils.*
import java.io.File
import java.net.URLDecoder

private const val profilesNumber = 1

fun main() {

    val defaultArgumentGenerationProfile = "java"

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
            (1 .. 10).forEach { _ ->
                val randomOperation = operations.random() ?: throw IllegalStateException()
                operations.scaleProbabilityInPlace(randomOperation, 1.2)
                chosenOperations.add(randomOperation)
            }
            println("Using ${chosenOperations.distinct().size} out of ${operations.size} operations for $groupName")
            val generated = propertiesTree.getKeys("groups", groupName, "generated")

            val typeVariables = propertiesTree.getKeys("groups", groupName, "variables")
                ?.associateWith { propertiesTree.getValues("groups", groupName, "variables", it)?.random()
                    ?: throw IllegalArgumentException("Variable of name $it doesn't exist in group $groupName!")
                } ?: throw IllegalStateException("Couldn't get variables mapping for $groupName group!")

            val protoArguments = chosenOperations.associateWith { generateArgumentsForProfile(groupName, defaultArgumentGenerationProfile, it, typeVariables, propertiesTree) }

            generated?.forEach { generatedName ->
                val possibleProfiles = mutableListOf(generatedName)
                val defaultProfile = propertiesTree.getValues("groups", groupName, "generated", generatedName)
                    ?: throw IllegalStateException("Couldn't get default profile reading for $generatedName!")
                possibleProfiles.addAll(defaultProfile)
                val benchmarkNotationEntries = chosenOperations.map { operation ->
                    val operationProfile = propertiesTree.getFirstMatchingKey(possibleProfiles, "groups", groupName, "operations", operation)
                    val arguments = mapArgumentsToProfile(groupName, operationProfile, operation, typeVariables, propertiesTree, protoArguments)
                    if (arguments.isEmpty()) "\${groupName.$groupName.operations.$operation.$operationProfile.content}"
                    else "#{groupName.$groupName.operations.$operation.$operationProfile.content, ${arguments.map { "${it.key} = ${it.value}"}.joinToString(", ")}}"
                }
                println("######### $generatedName #########")
                benchmarkNotationEntries.forEach { println(it) }
            }
        }
    }
}
