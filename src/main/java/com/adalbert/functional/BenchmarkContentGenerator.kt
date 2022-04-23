package com.adalbert.functional

import com.adalbert.utils.Tree
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.text.StringBuilder

object BenchmarkContentGenerator {

    data class BenchmarkMethod(val language: String, val generatedName: String, val generatedCode: String)
    data class BenchmarkInitialization(val collectionInit: String, val elementsFilling: List<String>)
    data class BenchmarkClass(val language: String, val className: String, val generatedCode: String)

    fun generateFullSourceFromSnippets(benchmarkName: String, groupName: String, benchmarkMethods: List<BenchmarkMethod>, context: MutableMap<String, List<String>>, propertiesTree: Tree, annotation: String = ""): List<BenchmarkClass> {
        val groupedByLanguage = benchmarkMethods.groupBy { it.language }
        return groupedByLanguage.keys.map { language ->
            val bob = StringBuilder()
            context["language"] = listOf(language)
            val annotationPart = if (annotation.isEmpty()) "" else "${annotation}_"
            val className = "${language[0].uppercase()}_${benchmarkName}${groupName}_${annotationPart}Benchmark"
            bob.appendLine("package com.adalbert;")
            propertiesTree.getValues("benchmarks", benchmarkName, "imports", language).forEach { bob.appendLine(it) }
            bob.appendLine("@State(Scope.Benchmark)")
            bob.appendLine("${ if (language == "java") "public " else "" } class $className {")
            propertiesTree.getValues("benchmarks", benchmarkName, "outerUnprocessed", language).forEach { bob.appendLine("\t$it") }
            val outerToProcess = propertiesTree.getValues("benchmarks", benchmarkName, "outerProcessed", language).joinToString("\n")
            bob.appendLine(BenchmarkContentProcessor.processBenchmarkText(outerToProcess, context, propertiesTree))
            groupedByLanguage[language]?.forEach { method -> stringifyMethod(bob, method) }
            bob.appendLine("}")
            BenchmarkClass(language,className, bob.toString())
        }
    }

    private val generalImports = mapOf(
        "java" to "import java.util.*;\n" +
                "import org.openjdk.jmh.annotations.*;\n" +
                "import org.openjdk.jmh.infra.*;\n",
        "scala" to "import java.util\n" +
                "import scala.collection.immutable\n" +
                "import scala.collection.mutable\n" +
                "import scala.reflect.classTag\n" +
                "import org.openjdk.jmh.annotations._\n" +
                "import org.openjdk.jmh.infra._\n",
    )

    fun generateFullSourceFromPolyaSnippets (
        groupName: String,
        method: BenchmarkMethod,
        initialization: BenchmarkInitialization,
        profileId: Int,
        identifier: String = ""
    ): BenchmarkClass {
        val timeOfExecution = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
        val identifierPart = identifier.ifEmpty { "_${method.generatedName}" }
        val className = "${method.language[0].uppercase()}_Profile${profileId}${groupName}${identifierPart}_$timeOfExecution"
        val eol = if (method.language == "java") ";" else ""
        val bob = StringBuilder()
        bob.appendLine("package com.adalbert;")
        bob.appendLine(
            generalImports[method.language]
            ?: throw IllegalStateException("Language ${method.language} not supported!"))
        bob.appendLine("@State(Scope.Benchmark)")
        bob.appendLine("${if (method.language == "java") "public " else ""}class $className {")
        bob.appendLine("\t${initialization.collectionInit}$eol")
        bob.appendLine("\t@Setup(Level.Invocation)")
        bob.appendLine("\t${methodsDeclarations[method.language to "setup"]!!(method.generatedName)}")
        bob.appendLine("\t\tcollection = ${initialization.collectionInit.substringAfter("=").trim()}$eol")
        initialization.elementsFilling.forEach { bob.appendLine("\t\t$it$eol") }
        bob.appendLine("\t}")
        stringifyMethod(bob, method, eol)
        bob.appendLine("}")
        return BenchmarkClass(method.language, className, bob.toString())
    }

    private fun stringifyMethod(bob: StringBuilder, method: BenchmarkMethod, lineEnding: String = "") {
        val (language, generatedName, generatedCode) = method
        bob.appendLine("\t@Benchmark")
        bob.appendLine("\t@Fork(1)")
        bob.appendLine("\t@Warmup(time=2)")
        bob.appendLine("\t@Measurement(time=1)")
        bob.appendLine("\t${methodsDeclarations[language to "init"]!!(generatedName)}")
        generatedCode.split("\n").forEach {
            bob.appendLine("\t\t$it$lineEnding")
        }
        bob.appendLine("\t}")
    }

    val methodsDeclarations = mapOf(
        Pair("java", "init") to { generatedName: String -> "public void $generatedName(Blackhole bh) {" },
        Pair("scala", "init")  to { generatedName: String -> "def $generatedName(bh: Blackhole): Unit = {" },
        Pair("java", "setup")  to { generatedName: String -> "public void setup$generatedName() {" },
        Pair("scala", "setup")  to { generatedName: String -> "def setup$generatedName(): Unit = {" },
    )

}