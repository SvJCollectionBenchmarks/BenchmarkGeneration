package com.adalbert.generation

import com.adalbert.generation.ArgumentsGenerator.generateArgumentsForProfile
import com.adalbert.generation.ArgumentsGenerator.mapArgumentsToProfile
import com.adalbert.utils.*
import java.io.File
import java.net.URLDecoder

private const val profilesNumber = 1

fun main() {

    val defaultArgumentGenerationProfile = "java"
    val defaultElementsCount = 100

    val resourcesUri = URLDecoder.decode(Tree("", mutableListOf()).javaClass.getResource("/")?.path, "UTF-8")
        ?: throw IllegalStateException("Couldn't find main resources folder")

    val groupsFiles = File("$resourcesUri/groups").listFiles()
        ?.filter { it.extension == "json" }
        ?: throw IllegalStateException("Couldn't load groups")

    val propertiesTree = Tree("root", mutableListOf(
        Tree("languages", mutableListOf(), mutableListOf("java", "scala")),
        Tree("groups", groupsFiles.map { file -> JSONTreeParser.parseJsonFile(file) }.toMutableList()),
    ))

    (0 until profilesNumber).forEach { _ ->
        val groups = propertiesTree.getKeys("groups")
        groups.forEach { groupName ->
            val operations: MutableMap<String, IntRange> = propertiesTree.getKeys("groups", groupName, "operations").toProbabilityMap()
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
                .associateWith { propertiesTree.getValues("groups", groupName, "variables", it).random() }

            val protoArguments = chosenOperations.associateWith { generateArgumentsForProfile(groupName, defaultArgumentGenerationProfile, it, typeVariables, propertiesTree) }

            val benchmarkClasses = generated.map { generatedName ->
                val possibleProfiles = mutableListOf(generatedName)
                val defaultProfile = propertiesTree.getValues("groups", groupName, "generated", generatedName)
                possibleProfiles.addAll(defaultProfile)
                val benchmarkNotationEntries = chosenOperations.map { operation ->
                    val operationProfile = propertiesTree.getFirstMatchingKey(possibleProfiles, "groups", groupName, "operations", operation)
                    val arguments = mapArgumentsToProfile(groupName, operationProfile, operation, typeVariables, propertiesTree, protoArguments)
                    val isConsumable = propertiesTree.getValue("groups", groupName, "operations", operation, operationProfile, "isConsumable").toBoolean()
                    val entry = if (arguments.isEmpty()) "\${groups.$groupName.operations.$operation.$operationProfile.content}"
                    else "#{groups.$groupName.operations.$operation.$operationProfile.content # ${arguments.map { "${it.key} = ${it.value}"}.joinToString(" # ")}}"
                    if (isConsumable) "bh.consume($entry)" else entry
                }
                val code = BenchmarkContentProcessor.processBenchmarkText(benchmarkNotationEntries.joinToString("\n"), mutableMapOf(), propertiesTree)
                // assuming here the default profile is the language
                val language = defaultProfile[0]
                val method = BenchmarkContentGenerator.BenchmarkMethod(language, generatedName, code)
                val collectionInit = propertiesTree.getValue("groups", groupName, "init", language, generatedName, "content")
                    .replaceVariablesWithValues(typeVariables)
                val operation = if (generatedName.contains("Map")) "put" else "add"
                val elementsFilling = (0 until defaultElementsCount).map {
                    val arguments = generateArgumentsForProfile(groupName, defaultProfile[0], operation, typeVariables, propertiesTree)
                    "#{groups.$groupName.operations.$operation.${defaultProfile[0]}.content # ${
                        arguments.map { "${it.key.name} = ${it.value}" }.joinToString(" # ")
                    }}"
                }.map {BenchmarkContentProcessor.processBenchmarkText(it, mutableMapOf(), propertiesTree)}
                val initialization = BenchmarkContentGenerator.BenchmarkInitialization(collectionInit, elementsFilling)
                BenchmarkContentGenerator.generateFullSourceFromPolyaSnippets(groupName, method, initialization, propertiesTree)
            }

            benchmarkClasses.forEach { println(it) }
        }
    }
}
