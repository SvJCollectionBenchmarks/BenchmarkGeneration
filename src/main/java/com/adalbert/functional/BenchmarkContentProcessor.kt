package com.adalbert.functional

import com.adalbert.utils.Tree
import java.io.File

object BenchmarkContentProcessor {

    private val languageTagRegex = Regex("<@([^@]*)@>")
    private val singleVariableRegex = Regex("\\$([a-zA-Z]*)")
    private val variableExpressionRegex = Regex("\\$\\{([^}]*)\\}")
    private val argumentExpressionRegex = Regex("\\#\\{([^#}]*(\\s*#[^\$#]*#)*)\\}")

    fun processBenchmarkFileContent(benchmarkFile: File, context: MutableMap<String, List<String>>, propertiesTree: Tree): Map<String, String> {
        val outputMap = mutableMapOf<String, String>()
        val languages = propertiesTree.getValues("languages")
        val benchmarkFileContent = benchmarkFile.readLines().joinToString("\n")
        val languageTags = languageTagRegex.findAll(benchmarkFileContent)
        languages.forEach { language ->
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
        val argumentsFragments = expression.substringAfter("#").substringBeforeLast("#").split("##").map { it.trim() }
        val argumentsList = mutableListOf<Pair<String, String>>()
        argumentsFragments.forEach { argument ->
            val argumentDefinition = argument.split("=").map { it.trim() }
            argumentsList.add(Pair(argumentDefinition[0], argumentDefinition[1]))
        }
        var subExpression = processExpressionWithVariables(expression.substringBefore("#").trim(), context, propertiesTree)
        argumentsList.forEach { subExpression = subExpression.replace("\$${it.first}", it.second) }
        return subExpression
    }

    private fun processExpressionWithVariables(expression: String, context: Map<String, List<String>>, propertiesTree: Tree): String {
        val fragments = expression.split(".").map { it.trim() }
        val previouslyMatched = mutableListOf<String>()
        fragments.forEach {
            if (it.matches(singleVariableRegex)) {
                val resolved = matchValueWithVariable(previouslyMatched, it.substring(1), context, propertiesTree)
                    ?: throw IllegalStateException("Couldn't process variable ${it.substring(1)} with the given context and previously matched $previouslyMatched")
                previouslyMatched.add(resolved)
            } else previouslyMatched.add(it)
        }
        return propertiesTree.getValues(previouslyMatched).joinToString(", ")
    }

    private fun matchValueWithVariable(previouslyMatched: List<String>, variable: String, context: Map<String, List<String>>, propertiesTree: Tree): String? {
        val values = context[variable] ?: return null
        if (values.size == 1) return values[0]
        val possibleTreeValues = propertiesTree.getKeys(previouslyMatched)
        return values.firstOrNull { possibleTreeValues.contains(it) }
    }
}