package com.adalbert.functional

import com.adalbert.utils.Tree
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.text.StringBuilder

object BenchmarkContentGenerator {

    data class BenchmarkMethod(val language: String, val generatedName: String, val generatedCode: String)
    data class BenchmarkInitialization(val collectionInit: String, val elementsFilling: List<String>)
    data class BenchmarkClass(val language: String, val className: String, val generatedCode: String)

    fun generateFullSourceFromSnippets(benchmarkName: String, groupName: String, benchmarkMethods: List<BenchmarkMethod>, propertiesTree: Tree): List<BenchmarkClass> {
        val groupedByLanguage = benchmarkMethods.groupBy { it.language }
        return groupedByLanguage.keys.map { language ->
            val bob = StringBuilder()
            val imports = propertiesTree.getValues("benchmarks", benchmarkName, "imports", language)
            val className = "${language[0].uppercase()}${benchmarkName}${groupName}Benchmark"
            bob.appendLine("package com.adalbert;")
            imports.forEach { bob.appendLine(it) }
            bob.appendLine("${if (language == "java") "public " else ""} class $className {")
            groupedByLanguage[language]?.forEach { method -> stringifyMethod(bob, method) }
            bob.appendLine("}")
            BenchmarkClass(language,className, bob.toString())
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
        val eol = if (method.language == "java") ";" else ""
        val bob = StringBuilder()
        bob.appendLine(
            generalImports[method.language]
            ?: throw IllegalStateException("Language ${method.language} not supported!"))
        bob.appendLine("@State(Scope.Benchmark)")
        bob.appendLine("${if (method.language == "java") "public " else ""}class $className {")
        bob.appendLine("\t${initialization.collectionInit}$eol")
        bob.appendLine("\t@Setup(Level.Invocation)")
        bob.appendLine("\t${methodsDeclarations[method.language to "setup"]!!(method.generatedName)}")
        initialization.elementsFilling.forEach { bob.appendLine("\t\t$it$eol") }
        bob.appendLine("\t}")
        stringifyMethod(bob, method, eol)
        bob.appendLine("}")
        return BenchmarkClass(method.language, className, bob.toString())
    }

    private fun stringifyMethod(bob: StringBuilder, method: BenchmarkMethod, lineEnding: String = "") {
        val (language, generatedName, generatedCode) = method
        bob.appendLine("\t@Benchmark")
        bob.appendLine("\t${methodsDeclarations[language to "init"]!!(generatedName)}")
        generatedCode.split("\n").forEach {
            bob.appendLine("\t\t$it$lineEnding")
        }
        bob.appendLine("\t}")
    }

    val methodsDeclarations = mapOf(
        Pair("java", "init") to { generatedName: String -> "public void test$generatedName(Blackhole bh) {" },
        Pair("scala", "init")  to { generatedName: String -> "def test$generatedName(bh: Blackhole): Unit = {" },
        Pair("java", "setup")  to { generatedName: String -> "public void setup$generatedName() {" },
        Pair("scala", "setup")  to { generatedName: String -> "def setup$generatedName(): Unit = {" },
    )

}