package com.adalbert.generation

import com.adalbert.utils.Tree

object ArgumentsGenerator {

    fun generateArguments(group: String, profiles: List<String>, operation: String, propertiesTree: Tree): Map<String, String> {
        val args = propertiesTree.getMatchingTree(profiles, "groups", group, "operations", operation).getKeys("args")
        return mapOf()
    }

}