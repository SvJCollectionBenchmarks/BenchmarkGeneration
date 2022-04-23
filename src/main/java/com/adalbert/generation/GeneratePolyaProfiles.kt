package com.adalbert.generation

import com.adalbert.functional.*
import com.adalbert.functional.ArgumentsGenerator.generateArgumentsForProfile
import com.adalbert.functional.ArgumentsGenerator.mapArgumentsToProfile
import com.adalbert.utils.*
import java.io.File
import java.net.URLDecoder
import java.nio.file.Path
import java.nio.file.Paths

private const val argumentGenerationProfile = "java"
private const val elementsCount = 1200
private const val operationsCount = 500
private const val profilesNumber = 5
private const val startingPolyaMultiplier = 1.3

private val baseCodeRoot: Path = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\generated\\multiOperationalPolya")
private val supportedLanguages = listOf("java", "scala")

private val additionOperations = mapOf("Map" to "put", "Sequence" to "append", "Set" to "add")

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

    val newCodeRoot = BenchmarkProjectHelper.generateProjectsInSupportedLanguages(baseCodeRoot, supportedLanguages)

    (0 until profilesNumber).forEach { profileId ->
        val groups = propertiesTree.getKeys("groups")
        groups.filter { propertiesTree.getValue("groups", it, "benchmarkedAutomatically") == "true" }.forEach { groupName ->
            val operations = propertiesTree.getKeys("groups", groupName, "operations").filter {
                propertiesTree.getValue("groups", groupName, "operations", it, "isBenchmarkedAutomatically") == "true"
            }.toProbabilityMap()
            val chosenOperations = mutableListOf<String>()
            (1 .. operationsCount).forEach { _ ->
                val randomOperation = operations.random() ?: throw IllegalStateException()
                val operationCount = chosenOperations.count { it == randomOperation }
                val realMultiplier =  1.0 + (startingPolyaMultiplier - 1.0) / (operationCount + 1)
                operations.scaleProbabilityInPlace(randomOperation, realMultiplier)
                chosenOperations.add(randomOperation)
            }
            println("Using ${chosenOperations.distinct().size} out of ${operations.size} operations for $groupName")
            println("Used operations are: ${chosenOperations.itemsPercentage()}")
            BenchmarkProjectHelper.writeNote("Used operations are: ${chosenOperations.itemsPercentage()}", "$groupName-${profileId}", newCodeRoot)
            val generated = propertiesTree.getKeys("groups", groupName, "generated")

            val typeVariables = propertiesTree.getKeys("groups", groupName, "variables")
                .associateWith { propertiesTree.getValues("groups", groupName, "variables", it).random() }

            val protoArguments = chosenOperations.mapIndexed {index, it -> index to it }.associateWith { generateArgumentsForProfile(groupName, argumentGenerationProfile, it.second, typeVariables, propertiesTree) }
            var fillingArgs: List<Map<ArgumentsGenerator.Argument, String>>? = null   // A little hacky, but fast

            val benchmarkClasses = generated.map { generatedName ->
                val possibleProfiles = mutableListOf(generatedName)
                val defaultProfile = propertiesTree.getValues("groups", groupName, "generated", generatedName)
                possibleProfiles.addAll(defaultProfile)
                val benchmarkNotationEntries = chosenOperations.mapIndexed { index, operation ->
                    val operationProfile = propertiesTree.getFirstMatchingKey(possibleProfiles, "groups", groupName, "operations", operation)
                    val arguments = mapArgumentsToProfile(groupName, operationProfile, operation, typeVariables, propertiesTree, index, protoArguments)
                    val isConsumable = propertiesTree.getValue("groups", groupName, "operations", operation, operationProfile, "isConsumable").toBoolean()
                    val entry = if (arguments.isEmpty()) "\${groups.$groupName.operations.$operation.$operationProfile.content}"
                    else "#{groups.$groupName.operations.$operation.$operationProfile.content # ${arguments.map { "${it.key} = ${it.value}"}.joinToString(" ## ")} #}"
                    if (isConsumable) "bh.consume($entry)" else entry
                }
                val code = BenchmarkContentProcessor.processBenchmarkText(benchmarkNotationEntries.joinToString("\n"), mutableMapOf(), propertiesTree)
                // assuming here the default profile is the language
                val language = defaultProfile[0]
                val method = BenchmarkContentGenerator.BenchmarkMethod(language, generatedName, code)
                val collectionInit = propertiesTree.getValue("groups", groupName, "init", language, generatedName, "content")
                    .replaceVariablesWithValues(typeVariables)
                val additionOperation = additionOperations[additionOperations.keys.firstOrNull { generatedName.contains(it) }]
                    ?: throw IllegalStateException("No addition operation for $generatedName!")
                fillingArgs = fillingArgs ?: (0 until elementsCount)
                    .map { generateArgumentsForProfile(groupName, defaultProfile[0], additionOperation, typeVariables, propertiesTree) }
                val elementsFilling = (0 until elementsCount).map {
                    "#{groups.$groupName.operations.$additionOperation.${defaultProfile[0]}.content # ${
                        fillingArgs?.get(it)?.map { "${it.key.name} = ${it.value}" }?.joinToString(" ## ")} #}"
                }.map { BenchmarkContentProcessor.processBenchmarkText(it, mutableMapOf(), propertiesTree)}
                val initialization = BenchmarkContentGenerator.BenchmarkInitialization(collectionInit, elementsFilling)
                BenchmarkContentGenerator.generateFullSourceFromPolyaSnippets(groupName, method, initialization, profileId)
            }

            BenchmarkProjectHelper.writeBenchmarkClasses(benchmarkClasses, newCodeRoot)
        }
    }
}
