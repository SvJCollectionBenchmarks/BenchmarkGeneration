package com.adalbert.functional

import com.adalbert.utils.add
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

object BenchmarkProjectHelper {

    private val javaVersion = "11"
    private val scalaVersion = "2.13.7"
    private val generationCommand = { language: String, generatedPostfix: String ->
        listOf(
            "cmd.exe", "/c", "mvn", "archetype:generate", "-DinteractiveMode=false", "-DarchetypeGroupId=org.openjdk.jmh",
            "-DarchetypeArtifactId=jmh-$language-benchmark-archetype", "-DgroupId=com.adalbert",
            "-DartifactId=jmh-$language-$generatedPostfix", "-Dversion=1.0"
        )
    }

    fun generateProjectInGivenLanguage(codeRoot: Path, language: String, postfix: String) {
        println("### Generating $language project... ###")
        ProcessBuilder()
            .command(generationCommand(language, postfix))
            .directory(codeRoot.toFile())
            .start().waitFor()
        val projectRoot = codeRoot.add("jmh-$language-$postfix")
        editPomSpec(projectRoot)
        val sourcesRoot = projectRoot.add("src\\main\\$language\\com\\adalbert")
        Files.delete(sourcesRoot.add("MyBenchmark.$language"))
    }

    private fun editPomSpec(projectRoot: Path) {
        val pomPath = projectRoot.add("pom.xml")
        val javaText = "<javac.target>$javaVersion</javac.target>\n" +
                "<maven.compiler.target>$javaVersion</maven.compiler.target>\n"+
                "<maven.compiler.source>$javaVersion</maven.compiler.source>"
        val scalaText = "<scala.stdLib.version>$scalaVersion</scala.stdLib.version>"
        val pomText =  Files.readAllLines(projectRoot.add("pom.xml")).joinToString("\n")
            .replace("<javac.target>1.8</javac.target>", javaText)
            .replace("<scala.stdLib.version>2.11.8</scala.stdLib.version>", scalaText)
        Files.write(pomPath, pomText.toByteArray(Charset.forName("UTF-8")))
    }

}