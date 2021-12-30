package com.adalbert.generation

import com.adalbert.utils.Tree
import com.adalbert.utils.substringUntilLast
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import java.io.File
import java.net.URLDecoder


fun traverse(root: JsonNode, builtTree: Tree) {
    if (root.isObject) {
        val fieldNames = root.fieldNames()
        while (fieldNames.hasNext()) {
            val fieldName = fieldNames.next()
            val newChild = Tree(fieldName, mutableListOf())
            builtTree.children.add(newChild)
            val fieldValue = root[fieldName]
            traverse(fieldValue, newChild)
        }
    } else if (root.isArray) {
        val arrayNode = root as ArrayNode
        for (i in 0 until arrayNode.size()) {
            val arrayElement = arrayNode[i]
            traverse(arrayElement, builtTree)
        }
    } else {
        if (builtTree.values == null) builtTree.values = mutableListOf()
        builtTree.values?.add(root.textValue())
    }
}

fun parseJsonFile(file: File): Tree {
    val outcomeTree = Tree(file.name.substringUntilLast("."), mutableListOf())
    traverse(ObjectMapper().readTree(file), outcomeTree)
    return outcomeTree
}

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
        Tree("groups", groupsFiles.map { file -> parseJsonFile(file) }.toMutableList()),
        Tree("benchmarks", benchmarksFiles.map { file -> parseJsonFile(file) }.toMutableList())
    ))

    val benchmarksTexts = File("$resourcesUri/benchmarks").listFiles()
        ?.filter { it.extension == "txt" }
        ?: throw IllegalStateException("Couldn't load benchmarks texts")

    benchmarksTexts.forEach { benchmarkFile ->
        val context: MutableMap<String, List<String>> = mutableMapOf()
        val benchmarkName = benchmarkFile.name.substringUntilLast(".")
        context["benchmark"] = listOf(benchmarkName)
        val groups = propertiesTree.getValues("benchmarks", benchmarkName, "groups")
        println("Groups for $benchmarkName are $groups")
    }
}