package com.adalbert.generation

import com.adalbert.utils.Tree
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.text.StringBuilder

object BenchmarkContentGenerator {

    data class BenchmarkMethod(val language: String, val generatedName: String, val generatedCode: String)
    data class BenchmarkInitialization(val collectionInit: String, val elementsFilling: String)
    data class BenchmarkClass(val language: String, val className: String, val generatedCode: String)

    // TODO: Change to Benchmark class, refactor
    fun generateFullSourceFromSnippets(benchmarkName: String, groupName: String, benchmarkMethods: List<BenchmarkMethod>, propertiesTree: Tree): Map<String, String> {
        val groupedByLanguage = benchmarkMethods.groupBy { it.language }
        return groupedByLanguage.keys.associateWith { language ->
            val bob = StringBuilder()
            val imports = propertiesTree.getValues("benchmarks", benchmarkName, "imports", language)
                ?: throw IllegalStateException("No imports declaration, presumably for $benchmarkName")
            imports.forEach { bob.appendLine(it) }
            bob.appendLine("class ${benchmarkName}${groupName}Benchmark {")
            groupedByLanguage[language]?.forEach { method -> stringifyMethod(bob, method) }
            bob.appendLine("}")
            bob.toString()
        }
    }

    private val generalImports = mapOf(
        "java" to "import java.util.*;\n" +
                "import scala.collection.immutable.*;\n" +
                "import scala.collection.mutable.*;\n" +
                "import scala.math.Ordering;\n" +
                "import org.openjdk.jmh.annotations.*;\n" +
                "import org.openjdk.jmh.infra.*;\n",
        "scala" to "import java.util\n" +
                "import scala.collection.immutable\n" +
                "import scala.collection.mutable\n" +
                "import org.openjdk.jmh.annotations\n" +
                "import org.openjdk.jmh.infra\n",
    )

    fun generateFullSourceFromPolyaSnippets (
        groupName: String,
        method: BenchmarkMethod,
        initialization: BenchmarkInitialization,
        propertiesTree: Tree
    ): BenchmarkClass {
        val timeOfExecution = LocalDateTime.now().format(DateTimeFormatter.ofPattern("_yyyyMMdd_HHmmssSSS_"))
        val className = "Polya$timeOfExecution${groupName}Benchmark"
        val bob = StringBuilder()
        bob.appendLine(generalImports[method.language]
            ?: throw IllegalStateException("Language ${method.language} not supported!"))
        bob.appendLine("@State(Scope.Benchmark)")
        bob.appendLine("${if (method.language == "java") "public " else ""}class $className {")
        bob.appendLine("\t${initialization.collectionInit}${if (method.language == "java") ";" else ""}")
        stringifyMethod(bob, method)
        bob.appendLine("}")
        return BenchmarkClass(method.language, className, bob.toString())
    }

    private fun stringifyMethod(bob: StringBuilder, method: BenchmarkMethod) {
        val (language, generatedName, generatedCode) = method
        bob.appendLine("\t@Benchmark")
        bob.appendLine("\t${methodsDeclarations[language]!!(generatedName)}")
        generatedCode.split("\n").forEach {
            bob.appendLine("\t\t$it${if (method.language == "java") ";" else ""}")
        }
        bob.appendLine("\t}")
    }

    val methodsDeclarations = mapOf(
        "java" to { generatedName: String -> "public void test$generatedName(Blackhole bh) {" },
        "scala" to { generatedName: String -> "def test$generatedName(bh: Blackhole): Unit = {" }
    )

}