package com.adalbert.generation

import com.adalbert.utils.Tree
import com.adalbert.utils.substringUntilLast
import java.io.File
import java.net.URLDecoder

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

    benchmarksTexts.forEach { benchmarkFile ->
        val context: MutableMap<String, List<String>> = mutableMapOf()
        val benchmarkName = benchmarkFile.name.substringUntilLast(".")
        context["benchmark"] = listOf(benchmarkName)
        val groups = propertiesTree.getValues("benchmarks", benchmarkName, "groups")
        groups?.forEach { groupName ->
            context["group"] = listOf(groupName)
            val generated = propertiesTree.getKeys("groups", groupName, "generated")
            generated?.forEach { generatedName ->
                val profiles = mutableListOf(generatedName)
                val defaultProfile = propertiesTree.getValues("groups", groupName, "generated", generatedName)
                    ?: throw IllegalStateException("Couldn't get default profile reading for $generatedName!")
                profiles.addAll(defaultProfile)
                context["profile"] = profiles
                println("############### ${profiles[0]} ###############")
                println(BenchmarkContentProcessor.processBenchmarkFileContent(benchmarkFile, context, propertiesTree)["java"])
            }
        }
    }
}