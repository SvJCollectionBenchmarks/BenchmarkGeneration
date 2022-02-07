package com.adalbert.generation

import com.adalbert.utils.Tree
import java.lang.StringBuilder

data class BenchmarkMethod(val language: String, val generatedName: String, val generatedCode: String)

object BenchmarkContentGenerator {

    fun generateFullSourceFromSnippets(benchmarkName: String, groupName: String, benchmarkMethods: List<BenchmarkMethod>, propertiesTree: Tree): Map<String, String> {
        val groupedByLanguage = benchmarkMethods.groupBy { it.language }
        return groupedByLanguage.keys.associateWith { language ->
            val bob = StringBuilder()
            val imports = propertiesTree.getValues("benchmarks", benchmarkName, "imports", language)
                ?: throw IllegalStateException("No imports declaration, presumably for $benchmarkName")
            imports.forEach { bob.appendLine(it) }
            bob.appendLine("class ${benchmarkName}${groupName}Benchmark {")
            groupedByLanguage[language]?.forEach { method ->
                bob.appendLine("\t@Benchmark")
                bob.appendLine("\t${methodsDeclarations[method.language]!!(method.generatedName)}")
                method.generatedCode.split("\n").forEach { bob.appendLine("\t\t$it") }
                bob.appendLine("\t}")
            }
            bob.appendLine("}")
            bob.toString()
        }
    }

    val methodsDeclarations = mapOf(
        "java" to { generatedName: String -> "public void test$generatedName(Blackhole bh) {" },
        "scala" to { generatedName: String -> "def test$generatedName(bh: Blackhole): Unit = {" }
    )

}