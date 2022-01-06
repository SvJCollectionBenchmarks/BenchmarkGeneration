package com.adalbert.generation

import com.adalbert.utils.Tree
import java.io.File
import java.net.URLDecoder

private const val profilesNumber = 1

fun main() {
    val resourcesUri = URLDecoder.decode(Tree("", mutableListOf()).javaClass.getResource("/")?.path, "UTF-8")
        ?: throw IllegalStateException("Couldn't find main resources folder")

    val groupsFiles = File("$resourcesUri/groups").listFiles()
        ?.filter { it.extension == "json" }
        ?: throw IllegalStateException("Couldn't load groups")

    val propertiesTree = Tree("root", mutableListOf(
        Tree("languages", mutableListOf(), mutableListOf("java", "scala")),
        Tree("groups", groupsFiles.map { file -> JSONTreeParser.parseJsonFile(file) }.toMutableList()),
    ))

    (0 until profilesNumber).forEach {
        val groups = propertiesTree.getKeys("groups")
        groups?.forEach { groupName ->
            val operations = propertiesTree.getKeys("groups", groupName, "operations")
            println("$groupName -> $operations")
        }
    }
}