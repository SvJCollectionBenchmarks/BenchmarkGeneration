package com.adalbert.generation

import com.adalbert.utils.Tree
import com.adalbert.utils.substringUntilLast
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import java.io.File
import java.net.URLDecoder
import kotlin.math.exp


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

fun processExpressionWithArguments(expression: String, context: MutableMap<String, List<String>>, propertiesTree: Tree): String {
    val fragments = expression.split(",").map { it.trim() }
    val argumentsList = mutableListOf<Pair<String, String>>()
    fragments.subList(1, fragments.size).forEach { argument ->
        val argumentDefinition = argument.split("=").map { it.trim() }
        argumentsList.add(Pair(argumentDefinition[0], argumentDefinition[1]))
    }
    var subExpression = processExpressionWithVariables(fragments[0], context, propertiesTree)
    argumentsList.forEach { subExpression = subExpression.replace("\$${it.first}", it.second) }
    return subExpression
}

fun processExpressionWithVariables(expression: String, context: Map<String, List<String>>, propertiesTree: Tree): String {
    val variableRegex = Regex("\\$([^\\.]*)")
    val fragments = expression.split(".").map { it.trim() }
    val previouslyMatched = mutableListOf<String>()
    fragments.forEach {
        if (it.matches(variableRegex)) {
            val resolved = matchValueWithVariable(previouslyMatched, it.substring(1), context, propertiesTree)
                ?: throw IllegalStateException("Couldn't process variable ${it.substring(1)} with the given context and previously matched $previouslyMatched")
            previouslyMatched.add(resolved)
        } else previouslyMatched.add(it)
    }
    return propertiesTree.getValues(previouslyMatched)?.joinToString(", ")
        ?: throw IllegalStateException("Values in the tree are null!")
}

fun matchValueWithVariable(previouslyMatched: List<String>, variable: String, context: Map<String, List<String>>, propertiesTree: Tree): String? {
    val values = context[variable] ?: return null
    if (values.size == 1) return values[0]
    val possibleTreeValues = propertiesTree.getKeys(previouslyMatched) ?: return null
    return values.firstOrNull { possibleTreeValues.contains(it) }
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
        groups?.forEach { groupName ->
            context["group"] = listOf(groupName)
            val generated = propertiesTree.getKeys("groups", groupName, "generated")
            generated?.forEach { generatedName ->
                val profiles = mutableListOf(generatedName)
                val defaultProfile = propertiesTree.getValues("groups", groupName, "generated", generatedName)
                    ?: throw IllegalStateException("Couldn't get default profile reading for $generatedName!")
                profiles.addAll(defaultProfile)
                context["profile"] = profiles
                processExpressionWithArguments("groups.\$group.operations.remove.\$profile.content, elem = 3", context, propertiesTree)
            }
        }
    }
}