package com.adalbert.generation

import com.adalbert.utils.Tree
import java.io.File

object BenchmarkContentProcessor {

    private val languageTagRegex = Regex("<@([^@]*)@>")
    private val variableExpressionRegex = Regex("\\$\\{([^}]*)\\}")
    private val argumentExpressionRegex = Regex("#\\{([^}]*)\\}")

    fun processBenchmarkFileContent(benchmarkFile: File, context: MutableMap<String, List<String>>, propertiesTree: Tree): Map<String, String> {
        val outputMap = mutableMapOf<String, String>()
        val languages = propertiesTree.getValues("languages")
        val benchmarkFileContent = benchmarkFile.readText()
        val languageTags = languageTagRegex.findAll(benchmarkFileContent)
        languages?.forEach { language ->
            val specificLanguageTags = languageTags.filter { it.groupValues[0].contains(language) }.sortedBy { it.range.first }.toList()
            val benchmarkText = benchmarkFileContent.substring(specificLanguageTags[0].range.last + 1, specificLanguageTags[1].range.first).trim()
            context["language"] = listOf(language)
            outputMap[language] = processBenchmarkText(benchmarkText, context, propertiesTree)
        }
        return outputMap
    }

    fun processBenchmarkText(benchmarkText: String, context: MutableMap<String, List<String>>, propertiesTree: Tree): String {
        var innerBenchmarkText = benchmarkText
        variableExpressionRegex.findAll(innerBenchmarkText).forEach {
            innerBenchmarkText = innerBenchmarkText.replace(
                it.groupValues[0],
                processExpressionWithVariables(it.groupValues[1], context, propertiesTree)
            )
        }
        argumentExpressionRegex.findAll(innerBenchmarkText).forEach {
            innerBenchmarkText = innerBenchmarkText.replace(
                it.groupValues[0],
                processExpressionWithArguments(it.groupValues[1], context, propertiesTree)
            )
        }
        return innerBenchmarkText
    }

    private fun processExpressionWithArguments(expression: String, context: MutableMap<String, List<String>>, propertiesTree: Tree): String {
        val fragments = expression.split("#").map { it.trim() }
        val argumentsList = mutableListOf<Pair<String, String>>()
        fragments.subList(1, fragments.size).forEach { argument ->
            val argumentDefinition = argument.split("=").map { it.trim() }
            argumentsList.add(Pair(argumentDefinition[0], argumentDefinition[1]))
        }
        var subExpression = processExpressionWithVariables(fragments[0], context, propertiesTree)
        argumentsList.forEach { subExpression = subExpression.replace("\$${it.first}", it.second) }
        return subExpression
    }

    private fun processExpressionWithVariables(expression: String, context: Map<String, List<String>>, propertiesTree: Tree): String {
        val variableRegex = Regex("\\$([a-zA-Z]*)")
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

    private fun matchValueWithVariable(previouslyMatched: List<String>, variable: String, context: Map<String, List<String>>, propertiesTree: Tree): String? {
        val values = context[variable] ?: return null
        if (values.size == 1) return values[0]
        val possibleTreeValues = propertiesTree.getKeys(previouslyMatched) ?: return null
        return values.firstOrNull { possibleTreeValues.contains(it) }
    }
}