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
        println("PANIC ARRAY")
        val arrayNode = root as ArrayNode
        for (i in 0 until arrayNode.size()) {
            val arrayElement = arrayNode[i]
            traverse(arrayElement, builtTree)
        }
    } else {
        builtTree.value = root.textValue()
    }
}

fun parseJsonFile(file: File) {
    val tree = Tree(file.name.substringUntilLast("."), mutableListOf())
    traverse(ObjectMapper().readTree(file), tree)
    println(tree)
}

fun main() {
    val resourcesUri = URLDecoder.decode(Context().javaClass.getResource("/")?.path, "UTF-8")
        ?: throw IllegalStateException("Couldn't find main resources folder")

    val benchmarks = File("$resourcesUri/benchmarks").listFiles()
        ?.filter { it.extension == "json" } ?: throw IllegalStateException("Couldn't load benchmarks")
    val groups = File("$resourcesUri/groups").listFiles()
        ?.filter { it.extension == "json" } ?: throw IllegalStateException("Couldn't load groups")

    val tree = Tree("root", mutableListOf(
        Tree("groups", groups.map { Tree(it.name.substringUntilLast("."), mutableListOf()) }.toMutableList()),
        Tree("benchmarks", benchmarks.map { Tree(it.name.substringUntilLast("."), mutableListOf()) }.toMutableList())
    ))

}