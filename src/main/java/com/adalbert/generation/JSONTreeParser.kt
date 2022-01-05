package com.adalbert.generation

import com.adalbert.utils.Tree
import com.adalbert.utils.substringUntilLast
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import java.io.File

object JSONTreeParser {

    fun parseJsonFile(file: File): Tree {
        val outcomeTree = Tree(file.name.substringUntilLast("."), mutableListOf())
        traverse(ObjectMapper().readTree(file), outcomeTree)
        return outcomeTree
    }

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

}