package com.adalbert.generation

import com.adalbert.functional.BenchmarkContentGenerator
import com.adalbert.functional.BenchmarkContentProcessor
import com.adalbert.functional.BenchmarkProjectHelper
import com.adalbert.functional.JSONTreeParser
import com.adalbert.utils.Tree
import com.adalbert.utils.substringUntilLast
import java.io.File
import java.net.URLDecoder
import java.nio.file.Path
import java.nio.file.Paths

private val baseCodeRoot: Path = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\generated\\multiOperationalOwn")
private val supportedLanguages = listOf("java", "scala")

enum class GenerationMode { CollectionPerMethod, CollectionPerClass }

fun main() {
    val resourcesUri = URLDecoder.decode(Tree("", mutableListOf()).javaClass.getResource("/")?.path, "UTF-8")
        ?: throw IllegalStateException("Couldn't find main resources folder")

    val benchmarksFiles = File("$resourcesUri/benchmarks").listFiles()
        ?.filter { it.extension == "json" }
        ?: throw IllegalStateException("Couldn't load benchmarks")
    val groupsFiles = File("$resourcesUri/groups").listFiles()
        ?.filter { it.extension == "json" }
        ?: throw IllegalStateException("Couldn't load groups")

    val propertiesTree = Tree("root", mutableListOf(
        Tree("languages", mutableListOf(), mutableListOf("java", "scala")),
        Tree("groups", groupsFiles.map { file -> JSONTreeParser.parseJsonFile(file) }.toMutableList()),
        Tree("benchmarks", benchmarksFiles.map { file -> JSONTreeParser.parseJsonFile(file) }.toMutableList())
    ))

    val benchmarksTexts = File("$resourcesUri/benchmarks").listFiles()
        ?.filter { it.extension == "txt" }
        ?: throw IllegalStateException("Couldn't load benchmarks texts")

    val newCodeRoot = BenchmarkProjectHelper.generateProjectsInSupportedLanguages(baseCodeRoot, supportedLanguages)

    benchmarksTexts.forEach { benchmarkFile ->
        val context: MutableMap<String, List<String>> = mutableMapOf()
        val benchmarkName = benchmarkFile.name.substringUntilLast(".")
        context["benchmark"] = listOf(benchmarkName)
        val groups = propertiesTree.getValues("benchmarks", benchmarkName, "groups")
        groups.forEach { groupName ->
            context["group"] = listOf(groupName)
            val generated = propertiesTree.getKeys("groups", groupName, "generated")
            val generationMode = propertiesTree.getValue("benchmarks", benchmarkName, "generateMode")
            val benchmarkMethods = generated.map { generatedName ->
                val profiles = mutableListOf(generatedName)
                val defaultProfile = propertiesTree.getValues("groups", groupName, "generated", generatedName).first()
                profiles.add(defaultProfile)
                // a project decision:
                val language = defaultProfile
                context["profile"] = profiles
                val code = BenchmarkContentProcessor.processBenchmarkFileContent(benchmarkFile, context, propertiesTree)[language]
                    ?: throw IllegalStateException("Couldn't generate methods code for $groupName and language $language!")
                BenchmarkContentGenerator.BenchmarkMethod(language, generatedName, code)
            }

            val benchmarkClasses = when (generationMode) {
                GenerationMode.CollectionPerMethod.name -> BenchmarkContentGenerator.generateFullSourceFromSnippets(benchmarkName, groupName, benchmarkMethods, context, propertiesTree)
                GenerationMode.CollectionPerClass.name ->
                    benchmarkMethods
                        .map { BenchmarkContentGenerator.generateFullSourceFromSnippets(benchmarkName, groupName, listOf(it), context, propertiesTree, it.generatedName) }
                        .flatten()
                else -> throw IllegalArgumentException("Unrecognized mode of operation: $generationMode")
            }

            BenchmarkProjectHelper.writeBenchmarkClasses(benchmarkClasses, newCodeRoot)

        }

    }
}